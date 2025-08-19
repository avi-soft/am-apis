package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.dto.EligibilityResult;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomProductGenderPhysicalRequirementRef;
import com.community.api.entity.CustomProductReserveCategoryBornBeforeAfterRef;
import com.community.api.entity.CustomStream;
import com.community.api.entity.CustomSubject;
import com.community.api.entity.Post;
import com.community.api.entity.Qualification;
import com.community.api.entity.QualificationDetails;
import com.community.api.entity.QualificationEligibility;
import com.community.api.entity.QualificationGroup;
import lombok.Getter;
import lombok.Setter;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class CartService {

    private EntityManager entityManager;

    // Others IDs

    public CartService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // Enum for eligibility status
    public enum EligibilityStatus {
        ELIGIBLE,
        NOT_ELIGIBLE,
        ELIGIBLE_WITH_WARNINGS
    }

    public boolean removeItemFromCart(Order cart, Long orderItemId) {
        List<OrderItem> items = cart.getOrderItems();
        Iterator<OrderItem> iterator = items.iterator();

        while (iterator.hasNext()) {
            OrderItem item = iterator.next();
            if (item.getId().equals(orderItemId)) {
                iterator.remove();
                entityManager.remove(item);
                entityManager.merge(cart);
                return true;
            }
        }
        return false;
    }



    public EligibilityResult checkCustomerEligibilityDetailed(CustomCustomer customer, CustomProduct product, boolean includeAllReasons) {
        EligibilityResult result = new EligibilityResult();

        if (customer == null || product == null) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Customer or product information is missing");
            return result;
        }

        List<Post> posts = product.getPosts();
        if (posts == null || posts.isEmpty()) {
            result.setStatus(EligibilityStatus.ELIGIBLE);
            return result;
        }

        boolean isEligible = true;
        boolean hasWarnings = false;

        for (Post post : posts) {
            // Check qualification eligibility
            EligibilityResult qualResult = checkQualificationEligibility(customer, post, includeAllReasons);
            if (qualResult.getStatus() == EligibilityStatus.NOT_ELIGIBLE) {
                isEligible = false;
                result.getReasons().addAll(qualResult.getReasons());
                if (!includeAllReasons) break;
            } else if (qualResult.getStatus() == EligibilityStatus.ELIGIBLE_WITH_WARNINGS) {
                hasWarnings = true;
                result.getWarnings().addAll(qualResult.getWarnings());
            }

            // Check age eligibility
            EligibilityResult ageResult = checkAgeEligibility(customer, post, includeAllReasons);
            if (ageResult.getStatus() == EligibilityStatus.NOT_ELIGIBLE) {
                isEligible = false;
                result.getReasons().addAll(ageResult.getReasons());
                if (!includeAllReasons) break;
            } else if (ageResult.getStatus() == EligibilityStatus.ELIGIBLE_WITH_WARNINGS) {
                hasWarnings = true;
                result.getWarnings().addAll(ageResult.getWarnings());
            }

            // Check physical eligibility
            EligibilityResult physicalResult = checkPhysicalEligibility(customer, post, includeAllReasons);
            if (physicalResult.getStatus() == EligibilityStatus.NOT_ELIGIBLE) {
                isEligible = false;
                result.getReasons().addAll(physicalResult.getReasons());
                if (!includeAllReasons) break;
            } else if (physicalResult.getStatus() == EligibilityStatus.ELIGIBLE_WITH_WARNINGS) {
                hasWarnings = true;
                result.getWarnings().addAll(physicalResult.getWarnings());
            }

            // Check religion eligibility
            EligibilityResult religionResult = checkReligionEligibility(customer, post, includeAllReasons);
            if (religionResult.getStatus() == EligibilityStatus.NOT_ELIGIBLE) {
                isEligible = false;
                result.getReasons().addAll(religionResult.getReasons());
                if (!includeAllReasons) break;
            } else if (religionResult.getStatus() == EligibilityStatus.ELIGIBLE_WITH_WARNINGS) {
                hasWarnings = true;
                result.getWarnings().addAll(religionResult.getWarnings());
            }

            // Check income eligibility
            EligibilityResult incomeResult = checkIncomeEligibility(customer, post, includeAllReasons);
            if (incomeResult.getStatus() == EligibilityStatus.NOT_ELIGIBLE) {
                isEligible = false;
                result.getReasons().addAll(incomeResult.getReasons());
                if (!includeAllReasons) break;
            } else if (incomeResult.getStatus() == EligibilityStatus.ELIGIBLE_WITH_WARNINGS) {
                hasWarnings = true;
                result.getWarnings().addAll(incomeResult.getWarnings());
            }
        }

        // Set final status
        if (!isEligible) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
        } else if (hasWarnings) {
            result.setStatus(EligibilityStatus.ELIGIBLE_WITH_WARNINGS);
        } else {
            result.setStatus(EligibilityStatus.ELIGIBLE);
        }

        return result;
    }

    private EligibilityResult checkQualificationEligibility(CustomCustomer customer, Post post, boolean includeAllReasons) {
        EligibilityResult result = new EligibilityResult();
        List<QualificationGroup> qualificationEligibilities = post.getQualificationEligibility();

        if (qualificationEligibilities == null || qualificationEligibilities.isEmpty()) {
            result.setStatus(EligibilityStatus.ELIGIBLE);
            return result;
        }

        List<QualificationDetails> customerQualifications = customer.getQualificationDetailsList();
        if (customerQualifications == null || customerQualifications.isEmpty()) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("No qualification details provided by customer");
            return result;
        }

        boolean hasMatchingQualification = false;
        boolean hasWarnings = false;

        for (QualificationGroup eligibility : qualificationEligibilities) {
            EligibilityResult singleQualResult = checkSingleQualificationEligibility(customerQualifications, eligibility, customer);

            if (singleQualResult.getStatus() == EligibilityStatus.ELIGIBLE) {
                hasMatchingQualification = true;
                if (!includeAllReasons) break;
            } else if (singleQualResult.getStatus() == EligibilityStatus.ELIGIBLE_WITH_WARNINGS) {
                hasMatchingQualification = true;
                hasWarnings = true;
                result.getWarnings().addAll(singleQualResult.getWarnings());
                if (!includeAllReasons) break;
            } else {
                result.getReasons().addAll(singleQualResult.getReasons());
            }
        }

        if (hasMatchingQualification) {
            result.setStatus(hasWarnings ? EligibilityStatus.ELIGIBLE_WITH_WARNINGS : EligibilityStatus.ELIGIBLE);
        } else {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            if (result.getReasons().isEmpty()) {
                result.addReason("No matching qualification found for this product");
            }
        }

        return result;
    }

    private EligibilityResult checkSingleQualificationEligibility(List<QualificationDetails> customerQualifications, QualificationGroup eligibility, CustomCustomer customer) {
        EligibilityResult result = new EligibilityResult();

        for (QualificationDetails qualification : customerQualifications) { 
            EligibilityResult qualResult = checkQualificationMatch(qualification, eligibility, customer);

            if (qualResult.getStatus() == EligibilityStatus.ELIGIBLE) {
                result.setStatus(EligibilityStatus.ELIGIBLE);
                return result;
            } else if (qualResult.getStatus() == EligibilityStatus.ELIGIBLE_WITH_WARNINGS) {
                result.setStatus(EligibilityStatus.ELIGIBLE_WITH_WARNINGS);
                result.getWarnings().addAll(qualResult.getWarnings());
                return result;
            }

            result.getReasons().addAll(qualResult.getReasons());
        }

        result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
        return result;
    }

    private EligibilityResult checkQualificationMatch(QualificationDetails qualification, QualificationGroup eligibility, CustomCustomer customer) {
        EligibilityResult result = new EligibilityResult();

        // Standard qualification matching first
        boolean qualificationMatches = false;
            for(QualificationEligibility req:eligibility.getQualificationGroups()) {
                qualificationMatches = true;
                for (Qualification qualificationEligibility : req.getQualifications()) {
                    if (!qualification.getQualification_id().equals(qualificationEligibility.getQualification_id())) {
                        qualificationMatches = false;
                        break;
                    } else if (qualification.getQualification_id().equals(Constant.BACHELORS_QUALIFICATION) || qualification.getQualification_id().equals(Constant.MASTERS_QUALIFICATION)) {
                        Qualification qualificationToFind = entityManager.find(Qualification.class, qualification.getQualification_id());
                        Qualification requiredQualificationToFind = entityManager.find(Qualification.class, qualificationEligibility.getQualification_id());
                        if (qualificationToFind != null && requiredQualificationToFind != null &&
                                qualificationToFind.getOverlap() != null &&
                                qualificationToFind.getOverlap().equals(requiredQualificationToFind.getOverlap())) {
                            qualificationMatches = true;
                            break;
                        }
                    }
                }

                System.out.println("matches or not");
                System.out.println(qualificationMatches);
                // If qualification matches, proceed with other checks without "Others" warnings
                if (qualificationMatches) {
                    // Check stream eligibility
                    EligibilityResult streamResult = checkStreamEligibilityForMatching(qualification, req);
                    if (streamResult.getStatus() == EligibilityStatus.NOT_ELIGIBLE) {
                        result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                        result.getReasons().addAll(streamResult.getReasons());
                        return result;
                    }

                    // Check subject eligibility
                    EligibilityResult subjectResult = checkSubjectEligibilityForMatching(qualification, req);
                    if (subjectResult.getStatus() == EligibilityStatus.NOT_ELIGIBLE) {
                        result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                        result.getReasons().addAll(subjectResult.getReasons());
                        return result;
                    }

                    // Check category eligibility
                    EligibilityResult categoryResult = checkCategoryEligibility(customer, req);
                    if (categoryResult.getStatus() == EligibilityStatus.NOT_ELIGIBLE) {
                        result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                        result.getReasons().addAll(categoryResult.getReasons());
                        return result;
                    }

                    // Check marks/CGPA eligibility
                    EligibilityResult marksResult = checkMarksEligibility(qualification, req);
                    if (marksResult.getStatus() == EligibilityStatus.NOT_ELIGIBLE) {
                        result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                        result.getReasons().addAll(marksResult.getReasons());
                        return result;
                    }

                    result.setStatus(EligibilityStatus.ELIGIBLE);
                    return result;
                }


                // If qualification doesn't match, check for "Others" scenarios for warnings
                // Check if any required qualification has "Others" ID
                boolean hasOthersInProduct = req.getQualifications().stream()
                        .anyMatch(q -> Constant.OTHERS_QUALIFICATION_ID.equals(q.getQualification_id()));

                if (hasOthersInProduct) {
                    result.setStatus(EligibilityStatus.ELIGIBLE_WITH_WARNINGS);
                    result.addWarning("Product requires 'Others' qualification. Please verify manually if your qualification matches the product requirements");
                    return result;
                }

                // Check if customer has "Others" qualification
                if (Constant.OTHERS_QUALIFICATION_ID.equals(qualification.getQualification_id())) {
                    // Check if any specific qualification matches first
                    boolean hasSpecificMatch = false;
                    for (Qualification requiredQual : req.getQualifications()) {
                        if (!Constant.OTHERS_QUALIFICATION_ID.equals(requiredQual.getQualification_id())) {
                            hasSpecificMatch = true;
                            break;
                        }
                    }

                    if (hasSpecificMatch) {
                        result.setStatus(EligibilityStatus.ELIGIBLE_WITH_WARNINGS);
                        result.addWarning("You have 'Others' qualification. Please verify manually if it matches the specific qualifications required for this product");
                        return result;
                    }
                }
            }

        // No match and no "Others" scenario
        result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
        result.addReason("Your qualification does not match the required qualifications for this product");
        return result;
    }

    // New method for stream checking when qualification already matches (no "Others" warnings)
    private EligibilityResult checkStreamEligibilityForMatching(QualificationDetails qualification, QualificationEligibility eligibility) {
        EligibilityResult result = new EligibilityResult();

        if (eligibility.getCustomStreams() == null || eligibility.getCustomStreams().isEmpty()) {
            result.setStatus(EligibilityStatus.ELIGIBLE);
            return result;
        }

        // Standard stream matching only (no "Others" warnings since qualification already matched)
        boolean streamMatches = false;
        for (CustomStream requiredStream : eligibility.getCustomStreams()) {
            System.out.println("required "+requiredStream.getStreamId());
            System.out.println("customer's "+qualification.getStream_id());
            if (qualification.getStream_id() != null &&
                    qualification.getStream_id().equals(requiredStream.getStreamId())) {
                streamMatches = true;
                break;
            }
        }
        System.out.println(streamMatches);
        if (!streamMatches) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Your stream does not match the required streams for this product");
        } else {
            result.setStatus(EligibilityStatus.ELIGIBLE);
        }

        return result;
    }

    // New method for subject checking when qualification already matches (no "Others" warnings)
    private EligibilityResult checkSubjectEligibilityForMatching(QualificationDetails qualification, QualificationEligibility eligibility) {
        EligibilityResult result = new EligibilityResult();

        if (eligibility.getCustomSubjects() == null || eligibility.getCustomSubjects().isEmpty()) {
            result.setStatus(EligibilityStatus.ELIGIBLE);
            return result;
        }

        if (qualification.getSubject_ids() == null || qualification.getSubject_ids().isEmpty()) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Subject information is required but not provided by customer");
            return result;
        }

        // Standard subject matching only (no "Others" warnings since qualification already matched)
        boolean subjectMatches = false;
        for (CustomSubject requiredSubject : eligibility.getCustomSubjects()) {
            if (qualification.getSubject_ids().contains(requiredSubject.getSubjectId())) {
                subjectMatches = true;
                break;
            }
        }

        if (!subjectMatches) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Your subjects do not match the required subjects for this product");
        } else {
            result.setStatus(EligibilityStatus.ELIGIBLE);
        }

        return result;
    }

    // Keep the original methods for when qualification doesn't match (for "Others" warnings)
    private EligibilityResult checkStreamEligibility(QualificationDetails qualification, QualificationEligibility eligibility) {
        EligibilityResult result = new EligibilityResult();

        if (eligibility.getCustomStreams() == null || eligibility.getCustomStreams().isEmpty()) {
            System.out.println("stream is empty ");
            result.setStatus(EligibilityStatus.ELIGIBLE);
            return result;
        }
        // Check if product requires "Others" stream
        boolean hasOthersInProduct = eligibility.getCustomStreams().stream()
                .anyMatch(s -> Constant.OTHERS_STREAM_ID.equals(s.getStreamId()));

        if (hasOthersInProduct) {
            System.out.println("means others product ");
            result.setStatus(EligibilityStatus.ELIGIBLE_WITH_WARNINGS);
            result.addWarning("Product requires 'Others' stream. Please verify manually if your stream matches the product requirements");
            return result;
        }

        // Check if customer has "Others" stream
        if (Constant.OTHERS_STREAM_ID.equals(qualification.getStream_id())) {
            System.out.println("yes others");
            result.setStatus(EligibilityStatus.ELIGIBLE_WITH_WARNINGS);
            result.addWarning("You have 'Others' stream. Please verify manually if it matches the specific streams required for this product");
            return result;
        }

        // Standard stream matching
        boolean streamMatches = false;
        for (CustomStream requiredStream : eligibility.getCustomStreams()) {
            if (qualification.getStream_id() != null &&
                    qualification.getStream_id().equals(requiredStream.getStreamId())) {
                System.out.println();
                streamMatches = true;
                break;
            }
        }

        if (!streamMatches) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Your stream does not match the required streams for this product");
        } else {
            result.setStatus(EligibilityStatus.ELIGIBLE);
        }

        return result;
    }

    private EligibilityResult checkSubjectEligibility(QualificationDetails qualification, QualificationEligibility eligibility) {
        EligibilityResult result = new EligibilityResult();

        if (eligibility.getCustomSubjects() == null || eligibility.getCustomSubjects().isEmpty()) {
            result.setStatus(EligibilityStatus.ELIGIBLE);
            return result;
        }

        if (qualification.getSubject_ids() == null || qualification.getSubject_ids().isEmpty()) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Subject information is required but not provided by customer");
            return result;
        }

        // Check if product requires "Others" subject
        boolean hasOthersInProduct = eligibility.getCustomSubjects().stream()
                .anyMatch(s -> Constant.OTHERS_SUBJECT_ID.equals(s.getSubjectId()));

        if (hasOthersInProduct) {
            result.setStatus(EligibilityStatus.ELIGIBLE_WITH_WARNINGS);
            result.addWarning("Product requires 'Others' subject. Please verify manually if your subjects match the product requirements");
            return result;
        }

        // Check if customer has "Others" subject
        if (qualification.getSubject_ids().contains(Constant.OTHERS_SUBJECT_ID)) {
            result.setStatus(EligibilityStatus.ELIGIBLE_WITH_WARNINGS);
            result.addWarning("You have 'Others' subject. Please verify manually if it matches the specific subjects required for this product");
            return result;
        }

        // Standard subject matching
        boolean subjectMatches = false;
        for (CustomSubject requiredSubject : eligibility.getCustomSubjects()) {
            if (qualification.getSubject_ids().contains(requiredSubject.getSubjectId())) {
                subjectMatches = true;
                break;
            }
        }

        if (!subjectMatches) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Your subjects do not match the required subjects for this product");
        } else {
            result.setStatus(EligibilityStatus.ELIGIBLE);
        }

        return result;
    }

    private EligibilityResult checkCategoryEligibility(CustomCustomer customer, QualificationEligibility eligibility) {
        EligibilityResult result = new EligibilityResult();

        if (eligibility.getCustomReserveCategory() == null || eligibility.getCustomReserveCategory().getReserveCategoryId().equals(Constant.RESERVED_CATEGORY_ALL)) {
            result.setStatus(EligibilityStatus.ELIGIBLE);
            return result;
        }

        if (customer.getCategory() == null) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Customer category is required but not provided");
            return result;
        }

        if(customer.getCategory().trim().equalsIgnoreCase("All"))
        {
            result.setStatus(EligibilityStatus.ELIGIBLE);
            return result;
        }

        // Check if product requires "Others" category
        if (Constant.OTHERS_CATEGORY_ID.equals(eligibility.getCustomReserveCategory().getReserveCategoryId())) {
            result.setStatus(EligibilityStatus.ELIGIBLE_WITH_WARNINGS);
            result.addWarning("Product requires 'Others' category. Please verify manually if your category matches the product requirements");
            return result;
        }

        // Check if customer has "Others" category
        if ("others".equalsIgnoreCase(customer.getCategory().trim())) {
            result.setStatus(EligibilityStatus.ELIGIBLE_WITH_WARNINGS);
            result.addWarning("You have 'Others' category. Please verify manually if it matches the specific category required for this product");
            return result;
        }

        // Standard category matching
        if (!eligibility.getCustomReserveCategory().getReserveCategoryName().equalsIgnoreCase(customer.getCategory())) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Your category (" + customer.getCategory() + ") does not match the required category (" +
                    eligibility.getCustomReserveCategory().getReserveCategoryName() + ") for this product");
        } else {
            result.setStatus(EligibilityStatus.ELIGIBLE);
        }

        return result;
    }

    private EligibilityResult checkMarksEligibility(QualificationDetails qualification, QualificationEligibility eligibility) {
        EligibilityResult result = new EligibilityResult();

        try {
            Boolean isAppearing = (eligibility.getIsAppearing() != null) ? eligibility.getIsAppearing() : false;
            Boolean isQualificationOngoing = (qualification.getQualificationIsOngoing() != null) ? qualification.getQualificationIsOngoing() : false;

            // Check appearing/ongoing status compatibility
            if (!isAppearing && isQualificationOngoing) {
                result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                result.addReason("This product requires completed qualification, but your qualification is ongoing");
                return result;
            } else if (isAppearing && !isQualificationOngoing) {
                result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                result.addReason("This product requires appearing/ongoing qualification, but your qualification is completed");
                return result;
            }

            // Check percentage requirements
            if (eligibility.getIsPercentage() != null && eligibility.getIsPercentage()) {
                return checkPercentage(qualification, eligibility, isAppearing);
            }
            else if(eligibility.getIsPercentage()!=null && !eligibility.getIsPercentage())
            {
                return checkCGPA(qualification,eligibility,isAppearing);
            }

            result.setStatus(EligibilityStatus.ELIGIBLE);
            return result;

        } catch (Exception e) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Error checking marks eligibility: " + e.getMessage());
            return result;
        }
    }

    private EligibilityResult checkPercentage(QualificationDetails qualification, QualificationEligibility eligibility, Boolean isAppearing) {
        EligibilityResult result = new EligibilityResult();

        // Check percentage requirement
        if (eligibility.getPercentage() != null) {

            if (!isAppearing) {
                // For completed qualifications, percentage is required
                if (qualification.getCumulative_percentage_value() == null) {
                    result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                    result.addReason("Percentage marks are required but not provided by customer");
                    return result;
                }

                if (eligibility.getPercentage() > qualification.getCumulative_percentage_value()) {
                    result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                    result.addReason("Your percentage (" + qualification.getCumulative_percentage_value() +
                            "%) is below the required minimum (" + eligibility.getPercentage() + "%)");
                    return result;
                }
            } else {
                // For appearing qualifications, if percentage is null, return warning
                if (qualification.getCumulative_percentage_value() == null) {
                    result.setStatus(EligibilityStatus.ELIGIBLE_WITH_WARNINGS);
                    result.addWarning("Percentage information not provided for appearing qualification. Please verify eligibility manually");
                    return result;
                }

                if (eligibility.getPercentage() > qualification.getCumulative_percentage_value()) {
                    result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                    result.addReason("Your percentage (" + qualification.getCumulative_percentage_value() +
                            "%) is below the required minimum (" + eligibility.getPercentage() + "%)");
                    return result;
                }
            }
        }
        result.setStatus(EligibilityStatus.ELIGIBLE);
        return result;
    }

    private EligibilityResult checkCGPA(QualificationDetails qualification, QualificationEligibility eligibility, Boolean isAppearing) {
        EligibilityResult result = new EligibilityResult();
        // Check CGPA requirement
        if (eligibility.getCgpa() != null) {
            if (!isAppearing) {
                // For completed qualifications
                if (qualification.getTotal_marks_type() == null) {
                    result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                    result.addReason("Marks type information is required but not provided");
                    return result;
                }

                if (qualification.getTotal_marks_type().equalsIgnoreCase("cgpa")) {
                    if (qualification.getMarks_obtained() == null) {
                        result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                        result.addReason("CGPA details are required but not provided by customer");
                        return result;
                    }

                    try {
                        double customerCGPA = Double.parseDouble(qualification.getMarks_obtained());
                        if (eligibility.getCgpa() > customerCGPA) {
                            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                            result.addReason("Your CGPA (" + customerCGPA +
                                    ") is below the required minimum (" + eligibility.getCgpa() + ")");
                            return result;
                        }
                    } catch (NumberFormatException e) {
                        result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                        result.addReason("Invalid CGPA format provided");
                        return result;
                    }
                } else if (qualification.getTotal_marks_type().equalsIgnoreCase("percentage")) {
                    if (qualification.getCumulative_cgpa_value() == null) {
                        result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                        result.addReason("CGPA details are required but not provided by customer");
                        return result;
                    }

                    if (eligibility.getCgpa() > qualification.getCumulative_cgpa_value()) {
                        result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                        result.addReason("Your CGPA (" + qualification.getCumulative_cgpa_value() +
                                ") is below the required minimum (" + eligibility.getCgpa() + ")");
                        return result;
                    }
                }
            } else {
                // For appearing qualifications
                if (qualification.getTotal_marks_type() == null) {
                    result.setStatus(EligibilityStatus.ELIGIBLE_WITH_WARNINGS);
                    result.addWarning("Marks type information not provided for appearing qualification. Please verify CGPA eligibility manually");
                    return result;
                }

                if (qualification.getTotal_marks_type().equalsIgnoreCase("cgpa")) {
                    if (qualification.getMarks_obtained() == null) {
                        result.setStatus(EligibilityStatus.ELIGIBLE_WITH_WARNINGS);
                        result.addWarning("CGPA details not provided for appearing qualification. Please verify eligibility manually");
                        return result;
                    }

                    try {
                        double customerCGPA = Double.parseDouble(qualification.getMarks_obtained());
                        if (eligibility.getCgpa() > customerCGPA) {
                            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                            result.addReason("Your CGPA (" + customerCGPA +
                                    ") is below the required minimum (" + eligibility.getCgpa() + ")");
                            return result;
                        }
                    } catch (NumberFormatException e) {
                        result.setStatus(EligibilityStatus.ELIGIBLE_WITH_WARNINGS);
                        result.addWarning("Invalid CGPA format provided for appearing qualification. Please verify eligibility manually");
                        return result;
                    }
                } else if (qualification.getTotal_marks_type().equalsIgnoreCase("percentage")) {
                    if (qualification.getCumulative_cgpa_value() == null) {
                        result.setStatus(EligibilityStatus.ELIGIBLE_WITH_WARNINGS);
                        result.addWarning("CGPA details not provided for appearing qualification. Please verify eligibility manually");
                        return result;
                    }

                    if (eligibility.getCgpa() > qualification.getCumulative_cgpa_value()) {
                        result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                        result.addReason("Your CGPA (" + qualification.getCumulative_cgpa_value() +
                                ") is below the required minimum (" + eligibility.getCgpa() + ")");
                        return result;
                    }
                }
            }
        }

        result.setStatus(EligibilityStatus.ELIGIBLE);
        return result;
    }

    private EligibilityResult checkAgeEligibility(CustomCustomer customer, Post post, boolean includeAllReasons) {
        EligibilityResult result = new EligibilityResult();
        List<CustomProductReserveCategoryBornBeforeAfterRef> ageRequirements = post.getAgeRequirement();

        if (ageRequirements == null || ageRequirements.isEmpty()) {
            result.setStatus(EligibilityStatus.ELIGIBLE);
            return result;
        }

        if (customer.getDob() == null || customer.getDob().trim().isEmpty()) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Date of birth is required for age eligibility check");
            return result;
        }

        if (customer.getGender() == null || customer.getGender().trim().isEmpty()) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Gender information is required for age eligibility check");
            return result;
        }

        if (customer.getCategory() == null || customer.getCategory().trim().isEmpty()) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Category information is required for age eligibility check");
            return result;
        }

        boolean hasMatchingAgeRequirement = false;
        boolean hasWarnings = false;

        for (CustomProductReserveCategoryBornBeforeAfterRef ageRequirement : ageRequirements) {
            EligibilityResult singleAgeResult = checkSingleAgeRequirement(customer, ageRequirement);

            if (singleAgeResult.getStatus() == EligibilityStatus.ELIGIBLE) {
                hasMatchingAgeRequirement = true;
                if (!includeAllReasons) break;
            } else if (singleAgeResult.getStatus() == EligibilityStatus.ELIGIBLE_WITH_WARNINGS) {
                hasMatchingAgeRequirement = true;
                hasWarnings = true;
                result.getWarnings().addAll(singleAgeResult.getWarnings());
                if (!includeAllReasons) break;
            } else {
                result.getReasons().addAll(singleAgeResult.getReasons());
            }
        }

        if (hasMatchingAgeRequirement) {
            result.setStatus(hasWarnings ? EligibilityStatus.ELIGIBLE_WITH_WARNINGS : EligibilityStatus.ELIGIBLE);
        } else {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            if (result.getReasons().isEmpty()) {
                result.addReason("Your age, gender, or category does not meet the requirements for this product");
            }
        }

        return result;
    }

    private EligibilityResult checkSingleAgeRequirement(CustomCustomer customer, CustomProductReserveCategoryBornBeforeAfterRef ageRequirement) {
        EligibilityResult result = new EligibilityResult();
        boolean hasWarnings = false;

        try {
            // Check gender eligibility with Others handling
            EligibilityResult genderResult = checkGenderEligibilityForAge(customer, ageRequirement);
            if (genderResult.getStatus() == EligibilityStatus.NOT_ELIGIBLE) {
                result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                result.getReasons().addAll(genderResult.getReasons());
                return result;
            } else if (genderResult.getStatus() == EligibilityStatus.ELIGIBLE_WITH_WARNINGS) {
                hasWarnings = true;
                result.getWarnings().addAll(genderResult.getWarnings());
            }

            // Check category eligibility with Others handling
            EligibilityResult categoryResult = checkCategoryEligibilityForAge(customer, ageRequirement);
            if (categoryResult.getStatus() == EligibilityStatus.NOT_ELIGIBLE) {
                result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                result.getReasons().addAll(categoryResult.getReasons());
                return result;
            } else if (categoryResult.getStatus() == EligibilityStatus.ELIGIBLE_WITH_WARNINGS) {
                hasWarnings = true;
                result.getWarnings().addAll(categoryResult.getWarnings());
            }

            // Check age eligibility based on the type of age requirement
            EligibilityResult ageResult;
            if (ageRequirement.getBornBeforeAfter() != null && ageRequirement.getBornBeforeAfter()) {
                ageResult = checkBornDateEligibility(customer, ageRequirement);
            } else {
                ageResult = checkMinMaxAgeEligibility(customer, ageRequirement);
            }

            if (ageResult.getStatus() == EligibilityStatus.NOT_ELIGIBLE) {
                result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                result.getReasons().addAll(ageResult.getReasons());
                return result;
            } else if (ageResult.getStatus() == EligibilityStatus.ELIGIBLE_WITH_WARNINGS) {
                hasWarnings = true;
                result.getWarnings().addAll(ageResult.getWarnings());
            }

            result.setStatus(hasWarnings ? EligibilityStatus.ELIGIBLE_WITH_WARNINGS : EligibilityStatus.ELIGIBLE);
            return result;

        } catch (Exception e) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Error checking age requirement: " + e.getMessage());
            return result;
        }
    }

    private EligibilityResult checkGenderEligibilityForAge(CustomCustomer customer, CustomProductReserveCategoryBornBeforeAfterRef ageRequirement) {
        EligibilityResult result = new EligibilityResult();

        if (ageRequirement.getGender() == null || ageRequirement.getGender().getGenderId().equals(Constant.GENDER_ALL)) {
            result.setStatus(EligibilityStatus.ELIGIBLE);
            return result;
        }

        String customerGender = customer.getGender().toLowerCase().trim();
        String requiredGenderName = ageRequirement.getGender().getGenderName().toLowerCase().trim();

        // Handle "Others" gender case
        if (Constant.OTHERS_GENDER_ID.equals(ageRequirement.getGender().getGenderId())) {
            result.setStatus(EligibilityStatus.ELIGIBLE_WITH_WARNINGS);
            result.addWarning("Product has 'Others' gender requirement. Please verify manually if your gender matches the product requirements");
            return result;
        }

        if ("others".equalsIgnoreCase(customerGender)) {
            result.setStatus(EligibilityStatus.ELIGIBLE_WITH_WARNINGS);
            result.addWarning("You have 'Others' gender. Please verify manually if it matches the specific gender required for this product");
            return result;
        }

        // Standard gender matching
        if (customerGender.equals(requiredGenderName) ||
                requiredGenderName.equalsIgnoreCase("no gender") ||
                requiredGenderName.equalsIgnoreCase("all")) {
            result.setStatus(EligibilityStatus.ELIGIBLE);
        } else {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Your gender (" + customer.getGender() + ") does not match the required gender (" +
                    ageRequirement.getGender().getGenderName() + ") for this age requirement");
        }

        return result;
    }

    private EligibilityResult checkCategoryEligibilityForAge(CustomCustomer customer, CustomProductReserveCategoryBornBeforeAfterRef ageRequirement) {
        EligibilityResult result = new EligibilityResult();

        if (ageRequirement.getCustomReserveCategory() == null || ageRequirement.getCustomReserveCategory().getReserveCategoryId().equals(Constant.RESERVED_CATEGORY_ALL)) {
            result.setStatus(EligibilityStatus.ELIGIBLE);
            return result;
        }

        String customerCategory = customer.getCategory().toLowerCase().trim();
        String requiredCategoryName = ageRequirement.getCustomReserveCategory().getReserveCategoryName().toLowerCase().trim();

        // Handle "Others" category case
        if (Constant.OTHERS_CATEGORY_ID.equals(ageRequirement.getCustomReserveCategory().getReserveCategoryId())) {
            result.setStatus(EligibilityStatus.ELIGIBLE_WITH_WARNINGS);
            result.addWarning("Product has 'Others' category requirement for age eligibility. Please verify manually if your category matches the product requirements");
            return result;
        }

        if ("others".equals(customerCategory)) {
            result.setStatus(EligibilityStatus.ELIGIBLE_WITH_WARNINGS);
            result.addWarning("You have 'Others' category. Please verify manually if it matches the specific category required for this product's age eligibility");
            return result;
        }

        // Standard category matching
        if (customerCategory.equalsIgnoreCase(requiredCategoryName) ||
                requiredCategoryName.equalsIgnoreCase("all")) {
            result.setStatus(EligibilityStatus.ELIGIBLE);
        } else {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Your category (" + customer.getCategory() + ") does not match the required category (" +
                    ageRequirement.getCustomReserveCategory().getReserveCategoryName() + ") for this age requirement");
        }

        return result;
    }

    private EligibilityResult checkBornDateEligibility(CustomCustomer customer, CustomProductReserveCategoryBornBeforeAfterRef ageRequirement) {
        EligibilityResult result = new EligibilityResult();

        try {
            SimpleDateFormat dobFormat = new SimpleDateFormat("dd-MM-yyyy");
            Date customerDob = dobFormat.parse(customer.getDob());

            if (ageRequirement.getBornAfter() != null) {
                if (!customerDob.after(ageRequirement.getBornAfter())) {
                    result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                    result.addReason("According to age eligibility criteria, your date of birth must be after " +
                            new SimpleDateFormat("dd-MM-yyyy").format(ageRequirement.getBornAfter())+ " but , your date of birth is too early i.e before the required born before  ");
                    return result;
                }
            }

            if (ageRequirement.getBornBefore() != null) {
                if (!customerDob.before(ageRequirement.getBornBefore())) {
                    result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                    result.addReason("According to age eligibility criteria,your date of birth must be before" +
                            new SimpleDateFormat("dd-MM-yyyy").format(ageRequirement.getBornBefore())+ " but your dob is late i.e after the required born after date");
                    return result;
                }
            }

            result.setStatus(EligibilityStatus.ELIGIBLE);
            return result;

        } catch (ParseException e) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Invalid date format for your date of birth: " + customer.getDob());
            return result;
        }
    }

    private EligibilityResult checkMinMaxAgeEligibility(CustomCustomer customer, CustomProductReserveCategoryBornBeforeAfterRef ageRequirement) {
        EligibilityResult result = new EligibilityResult();

        try {
            SimpleDateFormat dobFormat = new SimpleDateFormat("dd-MM-yyyy");
            Date customerDob = dobFormat.parse(customer.getDob());

            Date asOfDate = ageRequirement.getAsOfDate();
            if (asOfDate == null) {
                asOfDate = new Date();
            }

            int customerAge = calculateAge(customerDob, asOfDate);

            if (ageRequirement.getMinimumAge() != null) {
                if (customerAge < ageRequirement.getMinimumAge()) {
                    result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                    result.addReason("You are too young. Minimum age required is " + ageRequirement.getMinimumAge() +
                            " years, but You are " + customerAge + " years old");
                    return result;
                }
            }

            if (ageRequirement.getMaximumAge() != null) {
                if (customerAge > ageRequirement.getMaximumAge()) {
                    result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                    result.addReason("You are too old. Maximum age allowed is " + ageRequirement.getMaximumAge() +
                            " years, but You are " + customerAge + " years old");
                    return result;
                }
            }

            result.setStatus(EligibilityStatus.ELIGIBLE);
            return result;

        } catch (ParseException e) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Invalid date format for your date of birth: " + customer.getDob());
            return result;
        }
    }

    private EligibilityResult checkPhysicalEligibility(CustomCustomer customer, Post post, boolean includeAllReasons) {
        EligibilityResult result = new EligibilityResult();
        List<CustomProductGenderPhysicalRequirementRef> physicalRequirements = post.getPhysicalRequirements();

        if (physicalRequirements == null || physicalRequirements.isEmpty()) {
            result.setStatus(EligibilityStatus.ELIGIBLE);
            return result;
        }

        String customerGender = customer.getGender();
        if (customerGender == null) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Gender information is required for physical eligibility check");
            return result;
        }

        boolean hasMatchingGender = false;
        boolean hasWarnings = false;

        for (CustomProductGenderPhysicalRequirementRef requirement : physicalRequirements) {
            EligibilityResult genderResult = checkPhysicalGenderMatch(customerGender, requirement);

            if (genderResult.getStatus() == EligibilityStatus.ELIGIBLE ||
                    genderResult.getStatus() == EligibilityStatus.ELIGIBLE_WITH_WARNINGS) {

                hasMatchingGender = true;
                if (genderResult.getStatus() == EligibilityStatus.ELIGIBLE_WITH_WARNINGS) {
                    hasWarnings = true;
                    result.getWarnings().addAll(genderResult.getWarnings());
                }

                EligibilityResult measurementResult = checkPhysicalMeasurements(customer, requirement);
                if (measurementResult.getStatus() == EligibilityStatus.NOT_ELIGIBLE) {
                    result.getReasons().addAll(measurementResult.getReasons());
                    if (!includeAllReasons) {
                        result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
                        return result;
                    }
                } else {
                    result.setStatus(hasWarnings ? EligibilityStatus.ELIGIBLE_WITH_WARNINGS : EligibilityStatus.ELIGIBLE);
                    if (!includeAllReasons) return result;
                }
            }
        }

        if (!hasMatchingGender) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("No physical requirements found for your gender (" + customerGender + ")");
        } else if (result.getStatus() == null) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
        }

        return result;
    }

    private EligibilityResult checkPhysicalGenderMatch(String customerGender, CustomProductGenderPhysicalRequirementRef requirement) {
        EligibilityResult result = new EligibilityResult();

        if (requirement.getCustomGender() == null) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Physical requirement has no gender specified");
            return result;
        }

        if (customerGender.equalsIgnoreCase(requirement.getCustomGender().getGenderName()) || customerGender.equalsIgnoreCase("All")) {
            result.setStatus(EligibilityStatus.ELIGIBLE);
        } else {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Gender mismatch in physical requirements");
        }

        return result;
    }

    private EligibilityResult checkPhysicalMeasurements(CustomCustomer customer, CustomProductGenderPhysicalRequirementRef requirement) {
        EligibilityResult result = new EligibilityResult();
        Set<String> failedMeasurements = new HashSet<>();

        if (requirement.getHeight() != null) {
            if (customer.getHeightCms() == null) {
                failedMeasurements.add("Height information is required but not provided");
            } else if (customer.getHeightCms() < requirement.getHeight()) {
                failedMeasurements.add("Height requirement not met (Required: " + requirement.getHeight() +
                        " cm, Your height: " + customer.getHeightCms() + " cm)");
            }
        }

        if (requirement.getWeight() != null) {
            if (customer.getWeightKgs() == null) {
                failedMeasurements.add("Weight information is required but not provided");
            } else if (customer.getWeightKgs() < requirement.getWeight()) {
                failedMeasurements.add("Weight requirement not met (Required: " + requirement.getWeight() +
                        " kg, Your weight: " + customer.getWeightKgs() + " kg)");
            }
        }

        if (requirement.getChestSize() != null) {
            if (customer.getChestSizeCms() == null) {
                failedMeasurements.add("Chest size information is required but not provided");
            } else if (customer.getChestSizeCms() < requirement.getChestSize()) {
                failedMeasurements.add("Chest size requirement not met (Required: " + requirement.getChestSize() +
                        " cm, Your chest size: " + customer.getChestSizeCms() + " cm)");
            }
        }

        if (requirement.getWaistSize() != null) {
            if (customer.getWaistSizeCms() == null) {
                failedMeasurements.add("Waist size information is required but not provided");
            } else if (customer.getWaistSizeCms() < requirement.getWaistSize()) {
                failedMeasurements.add("Waist size requirement not met (Required: " + requirement.getWaistSize() +
                        " cm, Your waist size: " + customer.getWaistSizeCms() + " cm)");
            }
        }

        if (requirement.getShoeSize() != null) {
            if (customer.getShoeSizeInches() == null) {
                failedMeasurements.add("Shoe size information is required but not provided");
            } else if (customer.getShoeSizeInches() < requirement.getShoeSize()) {
                failedMeasurements.add("Shoe size requirement not met (Required: " + requirement.getShoeSize() +
                        " inches, Your shoe size: " + customer.getShoeSizeInches() + " inches)");
            }
        }

        if (!failedMeasurements.isEmpty()) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.setReasons(failedMeasurements);
        } else {
            result.setStatus(EligibilityStatus.ELIGIBLE);
        }

        return result;
    }

    private EligibilityResult checkReligionEligibility(CustomCustomer customer, Post post, boolean includeAllReasons) {
        EligibilityResult result = new EligibilityResult();
        List<String> requiredReligions = post.getReligion();

        if (requiredReligions == null || requiredReligions.isEmpty()) {
            result.setStatus(EligibilityStatus.ELIGIBLE);
            return result;
        }

        String customerReligion = customer.getReligion();
        if (customerReligion == null || customerReligion.trim().isEmpty()) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Religion information is required but not provided");
            return result;
        }

        // Check if product requires "Others" religion
        boolean hasOthersInProduct = requiredReligions.stream()
                .anyMatch(r -> "others".equalsIgnoreCase(r.trim()));

        if (hasOthersInProduct) {
            result.setStatus(EligibilityStatus.ELIGIBLE_WITH_WARNINGS);
            result.addWarning("Product accepts 'Others' religion. Please verify manually if your religion matches the product requirements");
            return result;
        }

        // Check if customer has "Others" religion
        if ("others".equalsIgnoreCase(customerReligion.trim())) {
            result.setStatus(EligibilityStatus.ELIGIBLE_WITH_WARNINGS);
            result.addWarning("You have 'Others' religion. Please verify manually if it matches the specific religions accepted for this product");
            return result;
        }

        // Standard religion matching
        boolean religionMatches = false;
        for (String requiredReligion : requiredReligions) {
            if (requiredReligion.trim().equalsIgnoreCase(customerReligion.trim())) {
                religionMatches = true;
                break;
            }
        }

        if (religionMatches) {
            result.setStatus(EligibilityStatus.ELIGIBLE);
        } else {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Your religion (" + customerReligion + ") is not accepted for this product. " +
                    "Required religions: " + String.join(", ", requiredReligions));
        }

        return result;
    }

    private EligibilityResult checkIncomeEligibility(CustomCustomer customer, Post post, boolean includeAllReasons) {
        EligibilityResult result = new EligibilityResult();
        Double incomeThreshold = post.getIncome();

        if (incomeThreshold == null) {
            result.setStatus(EligibilityStatus.ELIGIBLE);
            return result;
        }

        Long customerFamilyIncome = customer.getFamilyIncome();
        if (customerFamilyIncome == null) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Family income information is required but not provided");
            return result;
        }

        if (customerFamilyIncome > incomeThreshold) {
            result.setStatus(EligibilityStatus.NOT_ELIGIBLE);
            result.addReason("Your family income (₹" + customerFamilyIncome + ") does not meet the maximum requirement (₹" + incomeThreshold + ")");
        } else {
            result.setStatus(EligibilityStatus.ELIGIBLE);
        }

        return result;
    }

    private int calculateAge(Date birthDate, Date asOfDate) {
        Calendar birth = Calendar.getInstance();
        birth.setTime(birthDate);

        Calendar asOf = Calendar.getInstance();
        asOf.setTime(asOfDate);

        int age = asOf.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

        if (asOf.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }

    // Legacy method for backward compatibility
    public boolean isCustomerEligibleForProduct(CustomCustomer customer, CustomProduct product) {
        EligibilityResult result = checkCustomerEligibilityDetailed(customer, product, false);
        return result.getStatus() == EligibilityStatus.ELIGIBLE || result.getStatus() == EligibilityStatus.ELIGIBLE_WITH_WARNINGS;
    }
}