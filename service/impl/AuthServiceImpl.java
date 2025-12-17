package com.viswa.crm.service.impl;

import com.viswa.crm.dto.auth.CreateUserRequest;
import com.viswa.crm.dto.auth.LoginRequest;
import com.viswa.crm.dto.auth.LoginResponse;
import com.viswa.crm.dto.auth.UserResponse;
import com.viswa.crm.dto.auth.view.DealSummaryDto;
import com.viswa.crm.dto.auth.view.UserSummaryDto;
import com.viswa.crm.dto.auth.view.UserViewResponse;
import com.viswa.crm.model.Role;
import com.viswa.crm.model.User;
import com.viswa.crm.repository.DealRepository;
import com.viswa.crm.repository.RoleRepository;
import com.viswa.crm.repository.UserRepository;
import com.viswa.crm.service.AuthService;
import com.viswa.crm.util.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DealRepository dealRepository;

    @Override
    @Transactional(readOnly = true)  // Making read only for DB in the case of login
    public LoginResponse login(LoginRequest request) {

        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid username or password");
        }

        User user = userOpt.get();

        if (!PasswordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid username or password");
        }

        return mapToLoginResponse(user);
    }

    @Override
    public void logout(Long userId) {
        // No logic, since session handling is done in controller
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        Role role = resolveRole(request.getRoleId());

        User manager = null;

        if ("SALES".equals(role.getRoleName())) {
            if (request.getManagerId() == null) {
                throw new RuntimeException("Sales user must have a manager");
            }

            manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found"));

            if (!"MANAGER".equals(manager.getRole().getRoleName())) {
                throw new RuntimeException("Assigned manager is not a MANAGER");
            }
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPasswordHash(PasswordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setManager(manager);
        user.setCreatedAt(LocalDateTime.now());

        Long id = userRepository.save(user);
        user.setId(id);

        return mapToUserResponse(user);
    }


    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Using Optional API for chaining

        return mapToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    private Role resolveRole(Long roleId) {
        if (roleId != null) {
            return roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Invalid role"));
        }

        // setting SALES as default role
        return roleRepository.findByName("SALES")
                .orElseThrow(() -> new RuntimeException("Default role not found"));
    }

    private LoginResponse mapToLoginResponse(User user) {
        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .roleName(user.getRole().getRoleName())
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roleName(user.getRole().getRoleName())
                .managerId(user.getManager() != null ? user.getManager().getId() : null)
                .managerName(user.getManager() != null ? user.getManager().getFullName() : null)
                .build();
    }


    @Override
    @Transactional
    public void deleteUser(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }

        // 🚫 Block deletion if user has assigned deals
        if (dealRepository.existsByAssignedUserId(userId)) {
            throw new RuntimeException(
                    "Cannot delete user with assigned deals. Reassign deals first."
            );
        }

        userRepository.deleteById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserViewResponse getUserView(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<DealSummaryDto> deals =
                dealRepository.findByAssignedUserId(userId)
                        .stream()
                        .map(d -> DealSummaryDto.builder()
                                .id(d.getId())
                                .title(d.getTitle())
                                .status(d.getStatus().name())
                                .amount(d.getAmount())
                                .build())
                        .collect(Collectors.toList());

        List<UserSummaryDto> teamMembers = List.of();

        if ("MANAGER".equals(user.getRole().getRoleName())) {
            teamMembers =
                    userRepository.findUserIdsByManagerId(userId)
                            .stream()
                            .map(uid -> userRepository.findById(uid).orElse(null))
                            .filter(Objects::nonNull)
                            .map(this::mapToUserSummary)
                            .collect(Collectors.toList());
        }

        return UserViewResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roleName(user.getRole().getRoleName())
                .managerId(user.getManager() != null ? user.getManager().getId() : null)
                .managerName(user.getManager() != null ? user.getManager().getFullName() : null)
                .deals(deals)
                .teamMembers(teamMembers)
                .build();
    }

    private UserSummaryDto mapToUserSummary(User user) {
        return UserSummaryDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }


}
