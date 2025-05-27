package com.community.api.services;

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
import com.twilio.rest.numbers.v1.Eligibility;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
public class CartService {

private EntityManager entityManager;

   public  CartService(EntityManager entityManager)
   {
       this.entityManager= entityManager;
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

    public boolean isCustomerEligibleForProduct(CustomCustomer customer, CustomProduct product) {
        if (customer == null || product == null) {
            return false;
        }

        List<Post> posts = product.getPosts();
        if (posts == null || posts.isEmpty()) {
            return true;
        }

        for (Post post : posts) {
            if (!isCustomerQualificationEligibleForPost(customer, customer.getQualificationDetailsList(), post)) {
                throw new IllegalArgumentException("Your qualification does not satisfy the product's qualification Eligibility");
            }

            // Check age eligibility
            if (!isCustomerAgeEligible(customer, post)) {
                throw new IllegalArgumentException("Your age does not satisfy the product's age eligibility criteria");
            }

            // Check physical eligibility
            if (!isCustomerPhysicallyEligible(customer, post)) {
                throw new IllegalArgumentException("Your Physical Details does not satisfy the product's Physical Eligibility");
            }

            // Check religion eligibility
            if (!isCustomerReligionEligible(customer, post)) {
                throw new IllegalArgumentException("Your Religion details does not satisfy the product's Religion Eligibility");
            }

            // Check income eligibility
            if (!isCustomerIncomeEligible(customer, post)) {
                throw new IllegalArgumentException("Your Income details does not satisfy the product's Income Eligibility");
            }

            return true;
        }
        return false;
    }

    private boolean isCustomerQualificationEligibleForPost(CustomCustomer customer, List<QualificationDetails> customerQualifications, Post post) {
        List<QualificationEligibility> qualificationEligibilities = post.getQualificationEligibility();

        if (qualificationEligibilities == null || qualificationEligibilities.isEmpty()) {
            return true;
        }
        for (QualificationEligibility eligibility : qualificationEligibilities) {
            if (doesCustomerMeetQualificationEligibility(customerQualifications, eligibility,customer)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if any of the customer's qualifications meet a specific qualification eligibility
     */
    private boolean doesCustomerMeetQualificationEligibility(List<QualificationDetails> customerQualifications, QualificationEligibility eligibility,CustomCustomer customer) {
        if(customerQualifications ==null || customerQualifications.isEmpty())
        {
            throw new IllegalArgumentException("Customer's qualification details are empty");
        }
        for (QualificationDetails qualification : customerQualifications) {
            if (isQualificationEligible(qualification, eligibility,customer)) {
                return true;
            }
        }
        return false;
    }

    private boolean isQualificationEligible(QualificationDetails qualification, QualificationEligibility eligibility,CustomCustomer customer) {
        // 1. Check qualification type
        boolean qualificationMatches = false;
        for (Qualification requiredQualification : eligibility.getQualifications()) {
            if (qualification.getQualification_id().equals(requiredQualification.getQualification_id())) {
                qualificationMatches = true;
                break;
            }
            else if(qualification.getQualification_id().equals(3) || qualification.getQualification_id().equals(4))
            {
                Qualification qualificationToFind = entityManager.find(Qualification.class,qualification.getQualification_id());
                Qualification requiredQualificationToFind = entityManager.find(Qualification.class,requiredQualification.getQualification_id());
                if(qualificationToFind.getOverlap().equals(requiredQualificationToFind.getOverlap()))
                {
                    qualificationMatches= true;
                    break;
                }
            }
        }

        if (!qualificationMatches) {
            return false;
        }

        // 2. Check stream if applicable
        if (eligibility.getCustomStreams() != null && !eligibility.getCustomStreams().isEmpty()) {
            boolean streamMatches = false;
            for (CustomStream requiredStream : eligibility.getCustomStreams()) {
                if (qualification.getStream_id() != null &&
                        qualification.getStream_id().equals(requiredStream.getStreamId())) {
                    streamMatches = true;
                    break;
                }
            }

            if (!streamMatches) {
                return false; // Stream doesn't match
            }
        }

        // 3. Check subjects if applicable
        if (eligibility.getCustomSubjects() != null && !eligibility.getCustomSubjects().isEmpty()) {
            // If no subject IDs are provided by the customer, they don't meet this requirement
            if (qualification.getSubject_ids() == null || qualification.getSubject_ids().isEmpty()) {
                return false;
            }

            boolean subjectMatches = false;
            for (CustomSubject requiredSubject : eligibility.getCustomSubjects()) {
                if (qualification.getSubject_ids().contains(requiredSubject.getSubjectId())) {
                    subjectMatches = true;
                    break;
                }
            }

            if (!subjectMatches) {
                return false; // Subject doesn't match
            }
        }

        // 4. Check reserve category if applicable
        if (eligibility.getCustomReserveCategory() != null) {
            if(customer.getCategory()==null)
            {
                throw new IllegalArgumentException("Customer has not provided his category");
            }
            if(!eligibility.getCustomReserveCategory().getReserveCategoryName().equalsIgnoreCase(customer.getCategory()))
            {
                throw new IllegalArgumentException("Customer's category does not match the product's category in qualification");
            }
        }

        // 5. Check percentage/CGPA requirement
        if(eligibility.getIsAppearing()!=null && eligibility.getIsAppearing().equals(false))
        {
            if(eligibility.getIsPercentage()!=null && eligibility.getIsPercentage().equals(false))
            {
                return !qualification.getQualificationIsOngoing().equals(true);
            }
            else if(eligibility.getIsPercentage()!=null && eligibility.getIsPercentage().equals(true))
            {
                if(eligibility.getPercentage()!=null && eligibility.getPercentage()>qualification.getCumulative_percentage_value())
                {
                    return false;
                }
                else if(eligibility.getCgpa()!=null)
                {
                    if(qualification.getTotal_marks_type().equalsIgnoreCase("cgpa"))
                    {
                        return !(eligibility.getCgpa() > Double.parseDouble(qualification.getMarks_obtained()));
                    }
                    else if(qualification.getTotal_marks_type().equalsIgnoreCase("percentage"))
                    {
//                Here if user has not given his CGPA then need to discuss whether to autocalculate CGPA or not ???
                        throw new IllegalArgumentException("Customer has provided marks in percentage not in CGPA so can't check whether eligible for product or not");
                    }
                }
            }
        }

        //needs to apply whole condition for appearing qualification
        else if(eligibility.getIsAppearing()!=null && eligibility.getIsAppearing().equals(true))
        {
            if(eligibility.getIsPercentage()!=null && eligibility.getIsPercentage().equals(false))
            {
                return !qualification.getQualificationIsOngoing().equals(false);
            }
            else if(eligibility.getIsPercentage()!=null && eligibility.getIsPercentage().equals(true))
            {
                if(eligibility.getPercentage()!=null && qualification.getCumulative_percentage_value()==null)
                {
                    throw new IllegalArgumentException("Customer has not provided his Marks details so can't check for eligibility");
                }
                if(eligibility.getPercentage()!=null && eligibility.getPercentage()>qualification.getCumulative_percentage_value())
                {
                    return false;
                }
                else if(eligibility.getCgpa()!=null)
                {
                    if(qualification.getTotal_marks_type().equalsIgnoreCase("cgpa"))
                    {
                        if(qualification.getMarks_obtained()==null)
                        {
                            throw new IllegalArgumentException("Customer has not provided CGPA details so can't check whether eligible or not");
                        }
                        return !(eligibility.getCgpa() > Double.parseDouble(qualification.getMarks_obtained()));
                    }
                    else if(qualification.getTotal_marks_type().equalsIgnoreCase("percentage"))
                    {
//                Here if user has not given his CGPA then need to discuss whether to autocalculate CGPA or not ???
                        throw new IllegalArgumentException("Customer has provided marks in percentage not in CGPA so can't check whether eligible for product or not");
                    }
                }
            }
        }
        return true;
    }
    //Physical Eligibility
    private boolean isCustomerPhysicallyEligible(CustomCustomer customer, Post post) {
        List<CustomProductGenderPhysicalRequirementRef> physicalRequirements = post.getPhysicalRequirements();
        if (physicalRequirements == null || physicalRequirements.isEmpty()) {
            return true;
        }
        String customerGender = customer.getGender();
        if (customerGender == null) {
            throw new IllegalArgumentException("Gender is not provided in customer's profile");
        }
        for (CustomProductGenderPhysicalRequirementRef requirement : physicalRequirements) {
            if (isGenderMatching(customerGender, requirement)) {
                return checkPhysicalMeasurements(customer, requirement);
            }
        }
        return false;
    }

    private boolean isGenderMatching(String customerGender, CustomProductGenderPhysicalRequirementRef requirement) {
        if (requirement.getCustomGender() == null) {
            return false;
        }
        if(customerGender.equalsIgnoreCase(requirement.getCustomGender().getGenderName()))
        {
            return true;
            //here others case should be handled
        }
        return false;
    }
    private boolean checkPhysicalMeasurements(CustomCustomer customer, CustomProductGenderPhysicalRequirementRef requirement) {
        // Check height
        if (requirement.getHeight() != null) {
            if (customer.getHeightCms() == null || customer.getHeightCms() < requirement.getHeight()) {
                return false;
            }
        }
        // Check weight
        if (requirement.getWeight() != null) {
            if (customer.getWeightKgs() == null || customer.getWeightKgs() < requirement.getWeight()) {
                return false;
            }
        }
        // Check chest size
        if (requirement.getChestSize() != null) {
            if (customer.getChestSizeCms() == null || customer.getChestSizeCms() < requirement.getChestSize()) {
                return false;
            }
        }
        // Check waist size
        if (requirement.getWaistSize() != null) {
            if (customer.getWaistSizeCms() == null || customer.getWaistSizeCms() < requirement.getWaistSize()) {
                return false;
            }
        }
        // Check shoe size
        if (requirement.getShoeSize() != null) {
            if (customer.getShoeSizeInches() == null || customer.getShoeSizeInches() < requirement.getShoeSize()) {
                return false;
            }
        }
        return true;
    }

//Religion eligibility
    private boolean isCustomerReligionEligible(CustomCustomer customer, Post post) {
        List<String> requiredReligions = post.getReligion();
        if (requiredReligions == null || requiredReligions.isEmpty()) {
            return true;
        }
        String customerReligion = customer.getReligion();
        if (customerReligion == null || customerReligion.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer religion is required") ;
        }

        for (String requiredReligion : requiredReligions) {
            if (requiredReligion.trim().equalsIgnoreCase(customerReligion.trim())) {
                return true;
            }
        }

        //need to add check for other religion
//        if ("Others".equalsIgnoreCase(customerReligion) && customer.getOtherReligion() != null) {
//            for (String requiredReligion : requiredReligions) {
//                if (requiredReligion.trim().equalsIgnoreCase(customer.getOtherReligion().trim())) {
//                    return true;
//                }
//            }
//        }

        return false;
    }

//Income Eligibility
    private boolean isCustomerIncomeEligible(CustomCustomer customer, Post post) {
        Double incomeThreshold = post.getIncome();
        if (incomeThreshold == null) {
            return true;
        }

        Long customerFamilyIncome = customer.getFamilyIncome();
        if (customerFamilyIncome == null) {
            throw new IllegalArgumentException("Customer income is required when there's an income threshold");
        }
        return customerFamilyIncome > incomeThreshold;
//        return customerFamilyIncome <= incomeThreshold;
    }

    //Age Eligibility
    private boolean isCustomerAgeEligible(CustomCustomer customer, Post post) {
        List<CustomProductReserveCategoryBornBeforeAfterRef> ageRequirements = post.getAgeRequirement();

        if (ageRequirements == null || ageRequirements.isEmpty()) {
            return true; // No age restrictions
        }

        // Customer must have date of birth
        if (customer.getDob() == null || customer.getDob().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer's date of birth is required for age eligibility check");
        }

        // Customer must have gender
        if (customer.getGender() == null || customer.getGender().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer's gender is required for age eligibility check");
        }

        // Customer must have category
        if (customer.getCategory() == null || customer.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer's category is required for age eligibility check");
        }

        for (CustomProductReserveCategoryBornBeforeAfterRef ageRequirement : ageRequirements) {
            if (doesCustomerMeetAgeRequirement(customer, ageRequirement)) {
                return true;
            }
        }
        return false;
    }

    private boolean doesCustomerMeetAgeRequirement(CustomCustomer customer, CustomProductReserveCategoryBornBeforeAfterRef ageRequirement) {
        try {
            if (!isCustomerGenderEligible(customer, ageRequirement)) {
                return false;
            }

            if (!isCustomerCategoryEligible(customer, ageRequirement)) {
                return false;
            }

            // 3. Check age eligibility based on the type of age requirement
            if (ageRequirement.getBornBeforeAfter() != null && ageRequirement.getBornBeforeAfter()) {
                // Born before/after date validation
                return isCustomerBornDateEligible(customer, ageRequirement);
            } else {
                // Min/Max age validation
                return isCustomerMinMaxAgeEligible(customer, ageRequirement);
            }

        } catch (Exception e) {
            // Log the exception and return false
            System.err.println("Error checking age requirement: " + e.getMessage());
            return false;
        }
    }


    private boolean isCustomerGenderEligible(CustomCustomer customer, CustomProductReserveCategoryBornBeforeAfterRef ageRequirement) {
        if (ageRequirement.getGender() == null) {
            return true; // No gender restriction
        }

        String customerGender = customer.getGender().toLowerCase().trim();
        String requiredGenderName = ageRequirement.getGender().getGenderName().toLowerCase().trim();

        // Handle "Others" gender case
        if (ageRequirement.getGender().getGenderId().equals(3L)) { // Assuming 3 is Others
            if (ageRequirement.getGenderRunningField() != null &&
                    !ageRequirement.getGenderRunningField().trim().isEmpty()) {
                return customerGender.equalsIgnoreCase(ageRequirement.getGenderRunningField().trim());
            }
            return customerGender.equalsIgnoreCase("others");
        }

        // Standard gender matching
        return customerGender.equalsIgnoreCase(requiredGenderName) ||
                requiredGenderName.equalsIgnoreCase("no gender") ||
                requiredGenderName.equalsIgnoreCase("all");
    }

    private boolean isCustomerCategoryEligible(CustomCustomer customer, CustomProductReserveCategoryBornBeforeAfterRef ageRequirement) {
        if (ageRequirement.getCustomReserveCategory() == null) {
            return true; // No category restriction
        }

        String customerCategory = customer.getCategory().toLowerCase().trim();
        String requiredCategoryName = ageRequirement.getCustomReserveCategory().getReserveCategoryName().toLowerCase().trim();

        // Handle "Others" category case
        if (ageRequirement.getCustomReserveCategory().getReserveCategoryId().equals(6L)) { // Assuming 6 is Others
            if (ageRequirement.getCategoryRunningField() != null &&
                    !ageRequirement.getCategoryRunningField().trim().isEmpty()) {
                return customerCategory.equalsIgnoreCase(ageRequirement.getCategoryRunningField().trim());
            }
            return customerCategory.equals("others");
        }

        // Standard category matching
        return customerCategory.equals(requiredCategoryName) ||
                requiredCategoryName.equals("no category") ||
                requiredCategoryName.equals("all");
    }

    private boolean isCustomerBornDateEligible(CustomCustomer customer, CustomProductReserveCategoryBornBeforeAfterRef ageRequirement) {
        try {
            // Parse customer's date of birth
            SimpleDateFormat dobFormat = new SimpleDateFormat("dd-MM-yyyy");
            Date customerDob = dobFormat.parse(customer.getDob());

            // Check born after date (customer should be born after this date)
            if (ageRequirement.getBornAfter() != null) {
                if (!customerDob.after(ageRequirement.getBornAfter())) {
                    return false; // Customer born too early
                }
            }

            // Check born before date (customer should be born before this date)
            if (ageRequirement.getBornBefore() != null) {
                if (!customerDob.before(ageRequirement.getBornBefore())) {
                    return false; // Customer born too late
                }
            }

            return true;

        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format for customer's date of birth: " + customer.getDob());
        }
    }

    private boolean isCustomerMinMaxAgeEligible(CustomCustomer customer, CustomProductReserveCategoryBornBeforeAfterRef ageRequirement) {
        try {
            // Parse customer's date of birth
            SimpleDateFormat dobFormat = new SimpleDateFormat("dd-MM-yyyy");
            Date customerDob = dobFormat.parse(customer.getDob());

            // Get as of date for age calculation
            Date asOfDate = ageRequirement.getAsOfDate();
            if (asOfDate == null) {
                asOfDate = new Date(); // Use current date if as of date is not specified
            }

            // Calculate customer's age as of the specified date
            int customerAge = calculateAge(customerDob, asOfDate);

            // Check minimum age
            if (ageRequirement.getMinimumAge() != null) {
                if (customerAge < ageRequirement.getMinimumAge()) {
                    return false; // Customer too young
                }
            }

            // Check maximum age
            if (ageRequirement.getMaximumAge() != null) {
                if (customerAge > ageRequirement.getMaximumAge()) {
                    return false; // Customer too old
                }
            }

            return true;

        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format for customer's date of birth: " + customer.getDob());
        }
    }

    private int calculateAge(Date birthDate, Date asOfDate) {
        Calendar birth = Calendar.getInstance();
        birth.setTime(birthDate);

        Calendar asOf = Calendar.getInstance();
        asOf.setTime(asOfDate);

        int age = asOf.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

        // Adjust if birthday hasn't occurred this year
        if (asOf.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }

}
