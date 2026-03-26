package com.project.edusync.uis.service;

import com.project.edusync.uis.model.dto.profile.ComprehensiveUserProfileResponseDTO;
import com.project.edusync.uis.model.dto.profile.AddressDTO;
import com.project.edusync.uis.model.dto.profile.GuardianProfileDTO;
import com.project.edusync.uis.model.dto.profile.ProfileImageUploadCompleteRequestDTO;
import com.project.edusync.uis.model.dto.profile.ProfileImageUploadInitRequestDTO;
import com.project.edusync.uis.model.dto.profile.ProfileImageUploadInitResponseDTO;
import com.project.edusync.uis.model.dto.profile.StudentMedicalAllergyDTO;
import com.project.edusync.uis.model.dto.profile.StudentMedicalRecordDTO;
import com.project.edusync.uis.model.dto.profile.UserProfileDTO;
import com.project.edusync.uis.model.dto.profile.UserProfileUpdateDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProfileService {
    /**
     * Retrieves a user's profile by their authentication user_id.
     * @param userId The ID of the User (from the 'iam' module)
     * @return The complete UserProfileDTO
     */
    ComprehensiveUserProfileResponseDTO getProfileByUserId(Long userId);

    /**
     * Updates a user's profile by their authentication user_id.
     * @param userId The ID of the User (from the 'iam' module)
     * @param updateDto The DTO containing fields to update
     * @return The updated UserProfileDTO
     */
    UserProfileDTO updateProfileByUserId(Long userId, UserProfileUpdateDTO updateDto);

    /**
     * Creates provider-specific secure upload instructions for profile image upload.
     */
    ProfileImageUploadInitResponseDTO initiateProfileImageUpload(Long userId, ProfileImageUploadInitRequestDTO request);

    /**
     * Finalizes profile image upload and stores the delivered secure URL in UserProfile.
     */
    UserProfileDTO completeProfileImageUpload(Long userId, ProfileImageUploadCompleteRequestDTO request);

    AddressDTO addMyAddress(Long userId, AddressDTO request);

    AddressDTO updateMyAddress(Long userId, Long addressId, AddressDTO request);

    void deleteMyAddress(Long userId, Long addressId);

    StudentMedicalRecordDTO getMyMedicalRecord(Long userId);

    StudentMedicalRecordDTO createMyMedicalRecord(Long userId, StudentMedicalRecordDTO request);

    StudentMedicalRecordDTO updateMyMedicalRecord(Long userId, StudentMedicalRecordDTO request);

    StudentMedicalAllergyDTO addMyMedicalAllergy(Long userId, StudentMedicalAllergyDTO request);

    void deleteMyMedicalAllergy(Long userId, Long allergyId);

    List<GuardianProfileDTO> getMyGuardians(Long userId);
}
