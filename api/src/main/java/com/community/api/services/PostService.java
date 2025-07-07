
package com.community.api.services;
import javax.persistence.EntityManager;
import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.text.SimpleDateFormat;

import com.community.api.dto.*;
import com.community.api.entity.*;
import com.community.api.services.exception.ExceptionHandlingService;
import javassist.NotFoundException;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

    private final EntityManager entityManager;
    private final ExceptionHandlingService exceptionHandlingService;
    private final ReserveCategoryService reserveCategoryService;
    private final GenderService genderService;
    private final QualificationDetailsService qualificationDetailsService;
    private final SharedUtilityService sharedUtilityService;
    protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    protected SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
    @Autowired
    protected  ProductService productService;
    @Autowired
    private ProductReserveCategoryBornBeforeAfterRefService reserveCategoryBornBeforeAfterRefService;
    @Autowired
    private ProductReserveCategoryBornBeforeAfterRefService productReserveCategoryBornBeforeAfterRefService;

    public PostService(EntityManager entityManager, ExceptionHandlingService exceptionHandlingService,ReserveCategoryService reserveCategoryService,GenderService genderService,QualificationDetailsService qualificationDetailsService,SharedUtilityService sharedUtilityService) {
        this.entityManager = entityManager;
        this.exceptionHandlingService = exceptionHandlingService;
        this.reserveCategoryService = reserveCategoryService;
        this.genderService=genderService;
        this.qualificationDetailsService=qualificationDetailsService;
        this.sharedUtilityService=sharedUtilityService;
    }


    public List<Post> savePosts(List<PostDto>postDtos,Product product) throws Exception {
        try {
            List<Post> savedPosts = new ArrayList<>();

            for (PostDto postDto : postDtos) {
                Post post = savePost(postDto,product);
                savedPosts.add(post);
            }
            return savedPosts;
        }
        catch (IllegalArgumentException e)
        {
            exceptionHandlingService.handleException(e);
            throw new IllegalArgumentException(e.getMessage());
        }
        catch (Exception e) {
            exceptionHandlingService.handleException(e);
            throw new Exception("Failed to save Posts: " + e.getMessage(), e);
        }
    }
    public boolean validateAge(PostDto addProductDto) throws Exception {
        try {
            for(AddProductAgeDTO addProductAgeDTO:addProductDto.getReserveCategoryAge())
            {
            /*if (addProductDto.getReserveCategoryAge()==null) {
                throw new IllegalArgumentException("Reserve category cannot be empty.");
            }*/
            Set<Long> reserveCategoryId = new HashSet<>();
            Set<Integer>genderCategoryComboSet=new HashSet<>();

            Date currentDate = new Date(); // Current date for comparison
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);

            calendar.add(Calendar.YEAR, -105);
            Date minBornAfterDate = calendar.getTime();
            calendar.add(Calendar.YEAR, 100);
            Date maxBornBeforeDate = calendar.getTime();

                if(addProductAgeDTO.getBornBeofreAfter()==null)
                {
                    throw new IllegalArgumentException("born_before_after cannot be null");
                }
                if(addProductAgeDTO.getReserveCategory()!=6&&addProductAgeDTO.getCategoryRunningField()!=null)
                {
                    throw new IllegalArgumentException("cannot add running field for category except OTHERS");
                }
                else if (addProductAgeDTO.getReserveCategory() == 6 &&
                        (addProductAgeDTO.getCategoryRunningField() == null ||
                                addProductAgeDTO.getCategoryRunningField().trim().isEmpty())) {
                    throw new IllegalArgumentException("Running field is required when selecting 'Others' for category");
                }
                if(addProductAgeDTO.getGender()!=3&&addProductAgeDTO.getGenderRunningField()!=null)
                {
                    throw new IllegalArgumentException("cannot add running field for gender except OTHERS");
                }
                else if (addProductAgeDTO.getGender() == 3 &&
                        (addProductAgeDTO.getGenderRunningField() == null ||
                                addProductAgeDTO.getGenderRunningField().trim().isEmpty())) {
                    throw new IllegalArgumentException("Running field is required when selecting 'Others' for Gender");
                }
                if(!addProductAgeDTO.getBornBeofreAfter())
                {
                    if(addProductAgeDTO.getAsOfDate()==null)
                    {
                        throw new IllegalArgumentException("As of date cannot be null");
                    }
                    if(addProductAgeDTO.getMaxAge()==null||addProductAgeDTO.getMinAge()==null)
                        throw new IllegalArgumentException("Both minimum and maximum age re required");

                    qualificationDetailsService.validateDate(addProductAgeDTO.getAsOfDate(),"As of Date");
                }
                else {
                    if (addProductAgeDTO.getAsOfDate() == null) {
                        throw new IllegalArgumentException("As of date is required");
                    }
                    /*
                        calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
                        int currentYear = calendar.get(Calendar.YEAR);

                        calendar.set(Calendar.YEAR, currentYear);
                        calendar.set(Calendar.MONTH, Calendar.JANUARY);
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);

                        Date asOfDate = calendar.getTime();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));

                        String formattedDate = sdf.format(asOfDate);

                        addProductAgeDTO.setAsOfDate(formattedDate);
                    }*/ /*else
                    {
                        String input = addProductAgeDTO.getAsOfDate(); // "2006-01-16"
                        LocalDate localDate = LocalDate.parse(input); // parses ISO date
                         = java.sql.Date.valueOf(localDate);
                    }*/
                    try {
                        assert addProductAgeDTO.getBornBefore() != null;
                        assert addProductAgeDTO.getBornAfter() != null;
                    }catch (AssertionError e)
                    {
                        throw new IllegalArgumentException("Both born before and after dates are required");
                    }
                    int[]maxMin;
                        maxMin=sharedUtilityService.calculateAgeRange(addProductAgeDTO.getBornBefore(),addProductAgeDTO.getBornAfter(),dateFormat2.parse(addProductAgeDTO.getAsOfDate()));
                    addProductAgeDTO.setMaxAge(maxMin[1]);
                    addProductAgeDTO.setMinAge(maxMin[0]);
                }
                if (addProductAgeDTO.getReserveCategory() == null || addProductAgeDTO.getReserveCategory() <= 0) {
                    throw new IllegalArgumentException("Reserve category id cannot be null or <= 0.");
                }if (addProductAgeDTO.getGender() == null || addProductAgeDTO.getGender() <= 0) {
                    throw new IllegalArgumentException("Gender id cannot be null or <= 0.");
                }
                CustomGender gender=genderService.getGenderByGenderId(addProductAgeDTO.getGender());
                if(gender==null)
                    throw new NotFoundException("Invalid gender id");
                CustomReserveCategory category=reserveCategoryService.getReserveCategoryById(addProductAgeDTO.getReserveCategory());
                if(category==null)
                    throw new NotFoundException("Invalid category id");
                int genderAndCategoryCombo=(addProductAgeDTO.getReserveCategory().intValue())*10+(addProductAgeDTO.getGender().intValue());
              /*  if(gender.getGenderName().equals(Constant.NO_GENDER)&&category.getReserveCategoryName().equals(Constant.NO_CATEGORY)&&addProductDto.getReserveCategoryAge().size()>1)
                {
                    throw new IllegalArgumentException("This product is set to be category and gender independent, so no additional category/gender age can be applied.");
                }*/
                if(!genderCategoryComboSet.add(genderAndCategoryCombo))
                {
                    throw new IllegalArgumentException("Duplicate combination of gender and reserve category not allowed.");
                }
                reserveCategoryId.add(addProductAgeDTO.getReserveCategory());

                CustomReserveCategory reserveCategory = reserveCategoryService.getReserveCategoryById(addProductAgeDTO.getReserveCategory());
                if (reserveCategory == null) {
                    throw new IllegalArgumentException("Reserve category not found with id: " + addProductAgeDTO.getReserveCategory());
                }
               /* if (addProductDto.getReserveCategoryAge().getPost() == null) {
                    addProductDto.getReserveCategoryAge().setPost(Constant.DEFAULT_QUANTITY);
                } else if (addProductDto.getReserveCategoryAge().getPost() <= 0) {
                    throw new IllegalArgumentException(POSTLESSTHANORZERO);
                }*/

                if(addProductAgeDTO.getBornBeofreAfter().equals(true))
                {
                    if (addProductAgeDTO.getBornBefore() == null || addProductAgeDTO.getBornAfter() == null) {
                        throw new IllegalArgumentException("Born before date and born after date cannot be empty.");
                    }
                    dateFormat.parse(dateFormat.format(addProductAgeDTO.getBornAfter()));
                    dateFormat.parse(dateFormat.format(addProductAgeDTO.getBornBefore()));

                    if (!addProductAgeDTO.getBornBefore().before(new Date()) || !addProductAgeDTO.getBornAfter().before(new Date())) {
                        throw new IllegalArgumentException("Born before date and born after date must be of past.");
                    } else if (!addProductAgeDTO.getBornAfter().before(addProductAgeDTO.getBornBefore())) {
                        throw new IllegalArgumentException("Born after date must be past of born before date.");
                    }

                    if (addProductAgeDTO.getBornAfter().before(minBornAfterDate)) {
                        throw new IllegalArgumentException("Born after date cannot be more than 105 years in the past.");
                    }
                   /* if (addProductAgeDTO.getBornBefore().after(maxBornBeforeDate)) {
                        throw new IllegalArgumentException("Born before date must be at least 5 years in the past.");
                    }*/
                }

            }
            return true;
        } catch (NotFoundException | IllegalArgumentException notFoundException) {
            exceptionHandlingService.handleException(notFoundException);
            throw new IllegalArgumentException(notFoundException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception while validating reserve category: " + exception.getMessage());
        }
    }
    private Post savePost(PostDto postDto, Product product) throws Exception {
        Post post = new Post();
        post.setPostName(postDto.getPostName());
        post.setPostTotalVacancies(postDto.getPostTotalVacancies());
        if (postDto.getPostCode() != null) {
            post.setPostCode(postDto.getPostCode());
        }

        // list is created but haven't set it yet if there are no types
        List<VacancyDistributionType> vacancyTypes = new ArrayList<>();
        validateAge(postDto);

        // Only set vacancy types and handle distributions if there are vacancy distribution type IDs
        if (postDto.getVacancyDistributionTypeIds() != null && !postDto.getVacancyDistributionTypeIds().isEmpty()) {
            for (Integer typeId : postDto.getVacancyDistributionTypeIds()) {
                VacancyDistributionType type = entityManager.find(VacancyDistributionType.class, typeId);
                if (type == null) {
                    throw new IllegalArgumentException("Vacancy Distribution Type not found with id: " + typeId);
                }
                vacancyTypes.add(type);
            }
            post.setVacancyDistributionTypes(vacancyTypes);
        }

        post.setAdditionalComments(postDto.getPostAdditionalComments());
        post.setDuration(postDto.getDuration());
        post.setStateDistributionAdditionalComments(postDto.getStateDistributionAdditionalComments());
        post.setZoneDistributionAdditionalComments(postDto.getZoneDistributionAdditionalComments());
        post.setGenderDistributionAdditionalComments(postDto.getGenderDistributionAdditionalComments());
        post.setReligionAdditionalComments(postDto.getReligionAdditionalComments());
        post.setIncomeAdditionalComments(postDto.getIncomeAdditionalComments());
        post.setAdditionalEligibility(postDto.getAdditionalEligibility());
        post.setQualificationAdditionalComments(postDto.getQualificationAdditionalComments());
        post.setPhysicalAdditionalComments(postDto.getPhysicalAdditionalComments());
        post.setOtherDistributionAdditionalComments(postDto.getOtherDistributionAdditionalComments());
        post.setReserveCatAgeAdditionalComments(postDto.getReserveCatAgeAdditionalComments());
        post.setTotalSeatsVisible(postDto.getTotalSeatsVisible());
        post.setIncome(postDto.getIncome());
        post.setReligion(postDto.getReligion());

        // Persisting the post once, regardless of distribution types
        entityManager.persist(post);
        entityManager.flush();

        // Handle Gender Distribution if present
        if (postDto.getGenderWiseDistribution() != null) {
            updatePostGenderDistribution(postDto, post);
        }

        // Only handle other distributions if vacancy distribution types exist
        if (postDto.getVacancyDistributionTypeIds() != null && !postDto.getVacancyDistributionTypeIds().isEmpty()) {
            // Handle State distributions
            if (postDto.getVacancyDistributionTypeIds().contains(1) &&
                    postDto.getStateDistributions() != null &&
                    !postDto.getStateDistributions().isEmpty()) {
                List<StateDistribution> stateDistributions = saveStateDistributions(postDto.getStateDistributions(), post);
                post.setStateDistributions(stateDistributions);
            }

            // Handle Zone distributions
            if (postDto.getVacancyDistributionTypeIds().contains(2) &&
                    postDto.getZoneDistributions() != null &&
                    !postDto.getZoneDistributions().isEmpty()) {
                List<ZoneDistribution> zoneDistributions = saveZoneDistributions(postDto.getZoneDistributions(), post);
                post.setZoneDistributions(zoneDistributions);
            }
        }

        entityManager.persist(post);
        entityManager.flush(); // Ensure Post is saved and has an ID

        // Handle Qualification Eligibility Groups (NEW STRUCTURE)
        if (postDto.getQualificationEligibilityGroups() != null && !postDto.getQualificationEligibilityGroups().isEmpty()) {
            productService.validateQualificationEligibilityGroups(postDto.getQualificationEligibilityGroups());
            saveQualificationEligibilityGroups(postDto.getQualificationEligibilityGroups(), post);
        }

        // Handle Physical Requirements
        List<AddPhysicalRequirementDto> physicalRequirementDtos = postDto.getPhysicalRequirements();
        if (physicalRequirementDtos != null && !physicalRequirementDtos.isEmpty()) {
            for (AddPhysicalRequirementDto dto : physicalRequirementDtos) {
                CustomGender customGender = genderService.getGenderByGenderId(dto.getGenderId());

                CustomProductGenderPhysicalRequirementRef requirement = new CustomProductGenderPhysicalRequirementRef();
                requirement.setCustomGender(customGender);
                requirement.setHeight(dto.getHeight());
                requirement.setWeight(dto.getWeight());
                requirement.setGenderRunningfield(dto.getGenderRunningField());
                requirement.setShoeSize(dto.getShoeSize());
                requirement.setWaistSize(dto.getWaistSize());
                requirement.setChestSize(dto.getChestSize());
                requirement.setPost(post);
                requirement.setAdditionalComments(dto.getAdditionalComments());

                entityManager.persist(requirement);
            }
            entityManager.flush();
        }

        entityManager.refresh(post);

        // Handle Other Distributions
        if (postDto.getVacancyDistributionTypeIds() != null && postDto.getVacancyDistributionTypeIds().contains(4)) {
            if (postDto.getOtherDistributions() != null && !postDto.getOtherDistributions().isEmpty()) {
                List<OtherDistribution> otherDistributions = saveOtherDistributions(postDto.getOtherDistributions(), post);
                post.setOtherDistributions(otherDistributions);
            }
        }

        return post;
    }

    private void saveQualificationEligibilityGroups(List<QualificationEligibilityGroupDto> groupDtos, Post post) {
        for (QualificationEligibilityGroupDto groupDto : groupDtos) {
            // Create and save the group
            QualificationEligibilityGroup group = new QualificationEligibilityGroup();
            group.setPost(post);
            group.setGroupName(groupDto.getGroupName());
            group.setLogicalOperator(groupDto.getLogicalOperator());
            group.setCreatedAt(LocalDateTime.now());

            entityManager.persist(group);
            entityManager.flush(); // Ensure group is saved and has an ID

            // Save qualification eligibilities within the group
            for (QualificationEligibilityDto qualificationDto : groupDto.getQualificationEligibilities()) {
                if (qualificationDto != null) {
                    saveQualificationEligibility(qualificationDto, post, group);
                }
            }
        }
    }

    private void saveQualificationEligibility(QualificationEligibilityDto qualificationDto, Post post, QualificationEligibilityGroup group) {
        if (qualificationDto.getQualificationIds() != null && !qualificationDto.getQualificationIds().isEmpty()) {
            QualificationEligibility qualificationRequirement = new QualificationEligibility();

            // Set the group relationship
            qualificationRequirement.setGroup(group);
            qualificationRequirement.setPost(post);

            // Set qualifications
            List<Integer> qualificationIds = qualificationDto.getQualificationIds();
            List<Qualification> qualificationsToAdd = new ArrayList<>();
            if (qualificationIds != null) {
                for (Integer qualificationId : qualificationIds) {
                    Qualification qualification = entityManager.find(Qualification.class, qualificationId);
                    if (qualification == null) {
                        throw new IllegalArgumentException("Qualification not found with id: " + qualificationId);
                    }
                    qualificationsToAdd.add(qualification);
                }
                qualificationRequirement.setQualifications(qualificationsToAdd);
            }

            // Set subjects
            List<Long> subjectIds = qualificationDto.getCustomSubjectIds();
            if (subjectIds != null && !subjectIds.isEmpty()) {
                List<CustomSubject> subjectsToAdd = new ArrayList<>();
                for (Long subjectId : subjectIds) {
                    CustomSubject customSubject = entityManager.find(CustomSubject.class, subjectId);
                    if (customSubject == null) {
                        throw new IllegalArgumentException("CustomSubject not found with id: " + subjectId);
                    }
                    subjectsToAdd.add(customSubject);
                }
                qualificationRequirement.setCustomSubjects(subjectsToAdd);
            }

            // Set streams
            List<Long> streamIds = qualificationDto.getCustomStreamIds();
            if (streamIds != null && !streamIds.isEmpty()) {
                List<CustomStream> streamsToAdd = new ArrayList<>();
                for (Long streamId : streamIds) {
                    CustomStream customStream = entityManager.find(CustomStream.class, streamId);
                    if (customStream == null) {
                        throw new IllegalArgumentException("CustomStream not found with id: " + streamId);
                    }
                    streamsToAdd.add(customStream);
                }
                qualificationRequirement.setCustomStreams(streamsToAdd);
            }

            // Set reserve category
            if (qualificationDto.getCustomReserveCategoryId() != null) {
                CustomReserveCategory customReserveCategory = entityManager.find(CustomReserveCategory.class, qualificationDto.getCustomReserveCategoryId());
                if (customReserveCategory == null) {
                    throw new IllegalArgumentException("CustomReserveCategory not found with id: " + qualificationDto.getCustomReserveCategoryId());
                }
                qualificationRequirement.setCustomReserveCategory(customReserveCategory);
            }

            // Set other fields
            qualificationRequirement.setPercentage(qualificationDto.getPercentage());
            qualificationRequirement.setIsPercentage(qualificationDto.getIsPercentage());
            qualificationRequirement.setCgpa(qualificationDto.getCgpa());
            qualificationRequirement.setQualificationIdRunningField(qualificationDto.getQualificationIdRunningField());
            qualificationRequirement.setSubjectIdRunningField(qualificationDto.getSubjectIdRunningField());
            qualificationRequirement.setStreamIdRunningField(qualificationDto.getStreamIdRunningField());
            qualificationRequirement.setReserveCatIdRunningField(qualificationDto.getReserveCatIdRunningField());
            qualificationRequirement.setAdditionalComments(qualificationDto.getAdditionalComments());
            qualificationRequirement.setIsAppearing(qualificationDto.getIsAppearing());
            qualificationRequirement.setHighestQualificationSubjectNames(qualificationDto.getHighestQualificationSubjectNames());

            entityManager.persist(qualificationRequirement);
            entityManager.flush();

            // Save logical relationships if present
            if (qualificationDto.getLogicalRelationship() != null) {
                saveLogicalRelationships(qualificationDto.getLogicalRelationship(), qualificationRequirement);
            }
        }
    }

    private void saveLogicalRelationships(LogicalRelationshipDto relationshipDto, QualificationEligibility qualificationEligibility) {
        // Save stream relationship
        if (qualificationEligibility.getCustomStreams() != null && !qualificationEligibility.getCustomStreams().isEmpty()) {
            QualificationStreamRelationship streamRelationship = new QualificationStreamRelationship();
            streamRelationship.setQualificationEligibility(qualificationEligibility);
            streamRelationship.setLogicalOperator(relationshipDto.getStreamOperator());
//            streamRelationship.setMandatory(relationshipDto.getStreamsAreMandatory());
            entityManager.persist(streamRelationship);
        }

        // Save subject relationship
        if (qualificationEligibility.getCustomSubjects() != null && !qualificationEligibility.getCustomSubjects().isEmpty()) {
            QualificationSubjectRelationship subjectRelationship = new QualificationSubjectRelationship();
            subjectRelationship.setQualificationEligibility(qualificationEligibility);
            subjectRelationship.setLogicalOperator(relationshipDto.getSubjectOperator());
//            subjectRelationship.setMandatory(relationshipDto.getSubjectsAreMandatory());
            entityManager.persist(subjectRelationship);
        }
    }

    private List<StateDistribution> saveStateDistributions(List<StateDistributionDto> stateDtos, Post post) throws Exception {
        List<StateDistribution> stateDistributions = new ArrayList<>();

        for (StateDistributionDto stateDto : stateDtos) {
            StateDistribution stateDistribution = new StateDistribution();
            stateDistribution.setPost(post);

            StateCode stateCode = entityManager.find(StateCode.class, stateDto.getStateCodeId());
            if (stateCode == null) {
                throw new IllegalArgumentException("State not found with id: " + stateDto.getStateCodeId());
            }
            stateDistribution.setStateCode(stateCode);
            if(stateDto.getMaleVacancy()!=null&&stateDto.getFemaleVacancy()!=null&&stateDto.getTotalVacanciesInState()!=null) {
                if(stateDto.getMaleVacancy()+stateDto.getFemaleVacancy()!=stateDto.getTotalVacanciesInState())
                    throw new IllegalArgumentException("Total male and female vacancy in state is not equal to total vacancy");
                stateDistribution.setMaleVacancy(stateDto.getMaleVacancy());
                stateDistribution.setFemaleVacancy(stateDto.getFemaleVacancy());
                stateDistribution.setTotalVacanciesInState(stateDto.getTotalVacanciesInState());
            }
            stateDistribution.setIsDistrictDistribution(stateDto.getIsDistrictDistribution());

            if (Boolean.TRUE.equals(stateDto.getIsDistrictDistribution())) {
                // Calculate total state vacancies from districts
                Integer totalStateVacancies = calculateDistrictBasedStateVacancies(stateDto);
                stateDistribution.setTotalVacanciesInState(totalStateVacancies);
               stateDistribution.setAdditionalComments(stateDto.getAdditionalComments());
                entityManager.persist(stateDistribution);
                saveDistrictDistributions(stateDto, stateDistribution);
            } else {
                // Calculate and set state level vacancies
                stateDistribution.setAdditionalComments(stateDto.getAdditionalComments());
                saveStateLevelDistribution(stateDto, stateDistribution);
                entityManager.persist(stateDistribution);
            }
            stateDistributions.add(stateDistribution);
        }

        return stateDistributions;
    }

    private List<OtherDistribution> saveOtherDistributions(List<OtherDistribution> otherDistributions, Post post) {
        if (otherDistributions == null || post == null) {
            throw new IllegalArgumentException("Other distributions and post must not be null");
        }

        List<OtherDistribution> savedOtherDistributions = new ArrayList<>();

        for (OtherDistribution otherDistributionEntity : otherDistributions) {
            if (otherDistributionEntity == null) {
                continue;  // Skip null entries
            }

            try {
                OtherDistribution otherDistribution = new OtherDistribution();
                otherDistribution.setPost(post);
                // Validate and set total vacancy
                Long totalVacancy = otherDistributionEntity.getTotalVacancy();
                if (totalVacancy != null && totalVacancy >= 0) {
                    otherDistribution.setTotalVacancy(totalVacancy);
                } else {
                    throw new IllegalArgumentException("Total vacancy must be non-negative");
                }

                // Validate and set distribution value
                String distributionValue = otherDistributionEntity.getOtherDistributionValue();
                if (distributionValue != null && !distributionValue.trim().isEmpty()) {
                    otherDistribution.setOtherDistributionValue(distributionValue.trim());
                } else {
                    throw new IllegalArgumentException("Distribution value must not be empty");
                }

                entityManager.persist(otherDistribution);
                entityManager.flush();  // Flush after each persist to catch constraints early
                savedOtherDistributions.add(otherDistribution);

            } catch (ConstraintViolationException e) {
                throw new IllegalStateException("Constraint violation while saving other distribution: " +
                        otherDistributionEntity.getOtherDistributionValue(), e);
            } catch (Exception e) {
                throw new IllegalStateException("Error saving other distribution: " +
                        otherDistributionEntity.getOtherDistributionValue(), e);
            }
        }

        return savedOtherDistributions;
    }


    private Integer calculateDistrictBasedStateVacancies(StateDistributionDto stateDto) {
        return stateDto.getDistrictDistributions().stream()
                .mapToInt(district -> {
                    if (Boolean.TRUE.equals(district.getIsGenderWise())) {
                        return district.getMaleVacancy() + district.getFemaleVacancy();
                    } else if (!district.getCategoryDistributions().isEmpty()) {
                        return district.getCategoryDistributions().stream()
                                .mapToInt(DistrictCategoryDistributionDto::getVacancyCount)
                                .sum();
                    } else {
                        return district.getTotalVacancy();
                    }
                })
                .sum();
    }
    @Transactional
    public void updatePostAgeRequirements(List<PostDto> postDtos, CustomProduct product, List<Post>postList) {
        int i=0;
        List<List<CustomProductReserveCategoryBornBeforeAfterRef>>resultList=new ArrayList<>();
        for (Post post : postList) {
           reserveCategoryBornBeforeAfterRefService.saveBornBeforeAndBornAfter(postDtos.get(i).getReserveCategoryAge(),product,post);
            i++;
        }
    }

    private void saveDistrictDistributions(StateDistributionDto stateDto, StateDistribution stateDistribution) throws Exception {
        try {
            List<DistrictDistribution> districtDistributions = new ArrayList<>();

            for (DistrictDistributionDto districtDto : stateDto.getDistrictDistributions()) {
                DistrictDistribution districtDist = new DistrictDistribution();
                districtDist.setStateDistribution(stateDistribution);

                // Validate district exists
                Districts district = entityManager.find(Districts.class, districtDto.getDistrictId());
                if (district == null) {
                    throw new IllegalArgumentException("District not found with id: " + districtDto.getDistrictId());
                }
                districtDist.setDistrict(district);

                // Handle vacancy calculations
                districtDist.setIsGenderWise(districtDto.getIsGenderWise());
                if (Boolean.TRUE.equals(districtDto.getIsGenderWise())) {
                    districtDist.setMaleVacancy(districtDto.getMaleVacancy());
                    districtDist.setFemaleVacancy(districtDto.getFemaleVacancy());
                    districtDist.setTotalVacancy(districtDto.getMaleVacancy() + districtDto.getFemaleVacancy());
                } else if (districtDto.getCategoryDistributions() != null && !districtDto.getCategoryDistributions().isEmpty()) {
                    Integer totalVacancy = districtDto.getCategoryDistributions().stream()
                            .mapToInt(DistrictCategoryDistributionDto::getVacancyCount)
                            .sum();
                    districtDist.setTotalVacancy(totalVacancy);
                } else {
                    districtDist.setTotalVacancy(districtDto.getTotalVacancy());
                }
                districtDist.setAdditionalComment(districtDto.getAdditionalComment());
                if(districtDto.getDistrictId()!=786&&districtDto.getDistrictRunningField()!=null)
                    throw new IllegalArgumentException("Cannot add running field for district except OTHERS");
                if (districtDto.getDistrictId() == 786 &&
                        (districtDto.getDistrictRunningField() == null ||
                                districtDto.getDistrictRunningField().trim().isEmpty())) {
                    throw new IllegalArgumentException("Running field is required when selecting 'Others' for district");
                }
                districtDist.setDistrictRunningField(districtDto.getDistrictRunningField());
                // Persist the district distribution
                entityManager.persist(districtDist);

                // Save category distributions
                if (districtDto.getCategoryDistributions() != null) {
                    saveDistrictCategoryDistributions(districtDto.getCategoryDistributions(), districtDist);
                }

                districtDistributions.add(districtDist);
            }

            stateDistribution.setDistrictDistributions(districtDistributions);
        } catch (IllegalArgumentException e) {
            exceptionHandlingService.handleException(e);
            throw new IllegalArgumentException(e.getMessage());
        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            throw new Exception(e.getMessage());
        }
    }

    private void saveStateLevelDistribution(StateDistributionDto stateDto, StateDistribution stateDistribution) {
        stateDistribution.setIsGenderWise(stateDto.getIsGenderWise());

        if (Boolean.TRUE.equals(stateDto.getIsGenderWise())) {
            stateDistribution.setMaleVacancy(stateDto.getMaleVacancy());
            stateDistribution.setFemaleVacancy(stateDto.getFemaleVacancy());
            stateDistribution.setTotalVacanciesInState(stateDto.getMaleVacancy() + stateDto.getFemaleVacancy());
        }

        if (stateDto.getCategoryDistributions() != null) {
            List<CategoryDistribution> categoryDistributions = new ArrayList<>();
            Integer totalVacancies = 0;

            for (CategoryDistributionDto catDto : stateDto.getCategoryDistributions()) {
                CategoryDistribution catDist = new CategoryDistribution();
                catDist.setStateDistribution(stateDistribution);


                    CustomReserveCategory category = entityManager.find(CustomReserveCategory.class, catDto.getCategoryId());
                    if(catDto.getCategoryId()!=6&&catDto.getCategoryRunningField()!=null)
                        throw new IllegalArgumentException("Cannot add running field for Category except OTHERS");
                    else if (catDto.getCategoryId() == 6 &&
                            (catDto.getCategoryRunningField() == null ||
                                    catDto.getCategoryRunningField().trim().isEmpty())) {
                        throw new IllegalArgumentException("Running field is required when selecting 'Others' for category");
                    }
                    catDist.setCategory(category);
                catDist.setCategoryRunningField(catDto.getCategoryRunningField());
                catDist.setVacancyCount(catDto.getVacancyCount());
                catDist.setMaleVacancy(catDto.getMaleVacancy());
                catDist.setFemaleVacancy(catDto.getFemaleVacancy());
                catDist.setTotalVacancy(catDto.getTotalVacancy());
                totalVacancies += catDto.getVacancyCount();
                catDist.setAdditionalComment(catDto.getAdditionalComment());
                entityManager.persist(catDist);
                categoryDistributions.add(catDist);
            }

            // Explicitly set the category distributions list
            stateDistribution.setCategoryDistributions(categoryDistributions);

            if (!Boolean.TRUE.equals(stateDto.getIsGenderWise())) {
                stateDistribution.setTotalVacanciesInState(totalVacancies);
            }
        } else if (!Boolean.TRUE.equals(stateDto.getIsGenderWise())) {
            stateDistribution.setTotalVacanciesInState(stateDto.getTotalVacanciesInState());
        }
        if(!stateDto.getIsGenderWise().equals(true) && stateDto.getCategoryDistributions()!=null)
        {
            if(stateDto.getCategoryDistributions().isEmpty())
            {
                stateDistribution.setTotalVacanciesInState(stateDto.getTotalVacanciesInState());
            }
        }
        else if(!stateDto.getIsGenderWise().equals(true) && stateDto.getCategoryDistributions()==null)
        {
            stateDistribution.setTotalVacanciesInState(stateDto.getTotalVacanciesInState());
        }
    }

    private void saveDistrictCategoryDistributions(List<DistrictCategoryDistributionDto> dtos, DistrictDistribution districtDist) {
        for (DistrictCategoryDistributionDto dto : dtos) {
            DistrictCategoryDistribution categoryDist = new DistrictCategoryDistribution();
            categoryDist.setDistrictDistribution(districtDist);

            // Validate category exists
            CustomReserveCategory category = entityManager.find(CustomReserveCategory.class, dto.getCategoryId());
            if (category == null) {
                throw new IllegalArgumentException("Category not found with id: " + dto.getCategoryId());
            }
            if(dto.getCategoryId()!=6&&dto.getCategoryRunningField()!=null)
            {
                throw new IllegalArgumentException("Cannot add running field for category except OTHERS");
            }
            else if (dto.getCategoryId() == 6 &&
                    (dto.getCategoryRunningField() == null ||
                            dto.getCategoryRunningField().trim().isEmpty())) {
                throw new IllegalArgumentException("Running field is required when selecting 'Others' for category");
            }
            categoryDist.setCategoryRunningField(dto.getCategoryRunningField());
            categoryDist.setCategory(category);
            categoryDist.setAdditionalComment(dto.getAdditionalComment());
            categoryDist.setVacancyCount(dto.getVacancyCount());
            categoryDist.setMaleVacancy(dto.getMaleVacancy());
            categoryDist.setFemaleVacancy(dto.getFemaleVacancy());
            categoryDist.setTotalVacancy(dto.getTotalVacancy());
            // Persist and add to districtDistribution
            entityManager.persist(categoryDist);
            districtDist.getCategoryDistributions().add(categoryDist);
        }
    }


    private List<ZoneDistribution> saveZoneDistributions(List<ZoneDistributionDto> zoneDtos, Post post) throws Exception {
        List<ZoneDistribution> zoneDistributions = new ArrayList<>();

        for (ZoneDistributionDto zoneDto : zoneDtos) {
            ZoneDistribution zoneDistribution = new ZoneDistribution();
            zoneDistribution.setPost(post);

            Zone zone = entityManager.find(Zone.class, zoneDto.getZoneId());
            if (zone == null) {
                throw new IllegalArgumentException("Zone not found with id: " + zoneDto.getZoneId());
            }
            zoneDistribution.setZone(zone);
            zoneDistribution.setIsDivisionDistribution(zoneDto.getIsDivisionDistribution());
            zoneDistribution.setIsGenderWise(zoneDto.getIsGenderWise());
            zoneDistribution.setAdditionalComments(zoneDto.getAdditionalComments());
            zoneDistribution.setZoneRunningField(zoneDto.getZoneRunningField());
            if (Boolean.TRUE.equals(zoneDto.getIsDivisionDistribution())) {
                // Calculate total vacancies in the zone based on divisions
                Integer totalZoneVacancies = calculateDivisionBasedZoneVacancies(zoneDto);
                zoneDistribution.setTotalVacanciesInZone(totalZoneVacancies);
                zoneDistribution.setAdditionalComments(zoneDto.getAdditionalComments());
                if(zoneDto.getMaleVacancy()!=null&&zoneDto.getFemaleVacancy()!=null&&zoneDto.getTotalVacanciesInZone()!=null) {
                    if (zoneDto.getMaleVacancy() + zoneDto.getFemaleVacancy() != zoneDto.getTotalVacanciesInZone())
                        throw new IllegalArgumentException("Male vacancies and female vacancies sum is not equal to total for zone");
                }
                if(zoneDto.getMaleVacancy()!=null||zoneDto.getFemaleVacancy()!=null) {
                    zoneDistribution.setMaleVacancy(zoneDto.getMaleVacancy());
                    zoneDistribution.setFemaleVacancy(zoneDto.getFemaleVacancy());
                    if ((zoneDto.getMaleVacancy() != null || zoneDto.getFemaleVacancy() != null) && zoneDto.getTotalVacanciesInZone() == null)
                        throw new IllegalArgumentException("Need to provide total vacancy in zone");
                    if (zoneDto.getMaleVacancy() == null)
                        zoneDto.setMaleVacancy(0);
                    if (zoneDto.getFemaleVacancy() == null)
                        zoneDto.setFemaleVacancy(0);
                    zoneDistribution.setTotalVacanciesInZone(zoneDto.getTotalVacanciesInZone());
                    if (zoneDto.getMaleVacancy() + zoneDto.getFemaleVacancy() != zoneDto.getTotalVacanciesInZone())
                        throw new IllegalArgumentException("Total vacancy in zone is not equal to male and female vacancy");
                }
                zoneDistribution.setZoneRunningField(zoneDto.getZoneRunningField());
                entityManager.persist(zoneDistribution);
                saveDivisionDistributions(zoneDto, zoneDistribution);
            } else {
                // Handle zone-level distribution
                saveZoneLevelDistribution(zoneDto, zoneDistribution);
                entityManager.persist(zoneDistribution);
            }

            zoneDistributions.add(zoneDistribution);
        }

        return zoneDistributions;
    }
    private Integer calculateDivisionBasedZoneVacancies(ZoneDistributionDto zoneDto) {
        return zoneDto.getDivisionDistributions().stream()
                .mapToInt(division -> {
                    if (Boolean.TRUE.equals(division.getIsGenderWise())) {
                        return division.getMaleVacancy() + division.getFemaleVacancy();
                    } else if (!division.getCategoryDistributions().isEmpty()) {
                        return division.getCategoryDistributions().stream()
                                .mapToInt(DivisionCategoryDistributionDto::getVacancyCount)
                                .sum();
                    } else {
                        return division.getTotalVacancy();
                    }
                })
                .sum();
    }

    private void saveZoneLevelDistribution(ZoneDistributionDto zoneDto, ZoneDistribution zoneDistribution) {
        zoneDistribution.setIsGenderWise(zoneDto.getIsGenderWise());

        if (Boolean.TRUE.equals(zoneDto.getIsGenderWise())) {
            zoneDistribution.setMaleVacancy(zoneDto.getMaleVacancy());
            zoneDistribution.setFemaleVacancy(zoneDto.getFemaleVacancy());
            zoneDistribution.setTotalVacanciesInZone(zoneDto.getMaleVacancy() + zoneDto.getFemaleVacancy());
        }

        if (zoneDto.getCategoryDistributions() != null) {
            List<CategoryDistribution> categoryDistributions = new ArrayList<>();
            Integer totalVacancies = 0;

            for (CategoryDistributionDto catDto : zoneDto.getCategoryDistributions()) {
                CategoryDistribution catDist = new CategoryDistribution();
                catDist.setZoneDistribution(zoneDistribution);

                CustomReserveCategory category = entityManager.find(CustomReserveCategory.class, catDto.getCategoryId());
                if (category == null) {
                    throw new IllegalArgumentException("Category not found with id: " + catDto.getCategoryId());
                }
                catDist.setCategory(category);
                catDist.setVacancyCount(catDto.getVacancyCount());
                catDist.setMaleVacancy(catDto.getMaleVacancy());
                catDist.setFemaleVacancy(catDto.getFemaleVacancy());
                catDist.setTotalVacancy(catDto.getTotalVacancy());
                totalVacancies += catDto.getVacancyCount();
                catDist.setAdditionalComment(catDto.getAdditionalComment());
                entityManager.persist(catDist);
                categoryDistributions.add(catDist);
            }

            // Explicitly set the category distributions list
            zoneDistribution.setCategoryDistributions(categoryDistributions);
            zoneDistribution.setAdditionalComments(zoneDistribution.getAdditionalComments());

            if (!Boolean.TRUE.equals(zoneDto.getIsGenderWise())) {
                zoneDistribution.setTotalVacanciesInZone(totalVacancies);
            }
        } else if (!Boolean.TRUE.equals(zoneDto.getIsGenderWise())) {
            zoneDistribution.setTotalVacanciesInZone(zoneDto.getTotalVacanciesInZone());
        }

        if(!zoneDto.getIsGenderWise().equals(true) && zoneDto.getCategoryDistributions()!=null)
        {
            if(zoneDto.getCategoryDistributions().isEmpty())
            {
                zoneDistribution.setTotalVacanciesInZone(zoneDto.getTotalVacanciesInZone());
            }
        }
        else if(!zoneDto.getIsGenderWise().equals(true) && zoneDto.getCategoryDistributions()==null)
        {
            zoneDistribution.setTotalVacanciesInZone(zoneDto.getTotalVacanciesInZone());
        }
    }

    private void saveDivisionDistributions(ZoneDistributionDto zoneDto, ZoneDistribution zoneDistribution) throws Exception {
        try {
            List<DivisionDistribution> divisionDistributions = new ArrayList<>();

            for (DivisionDistributionDto divisionDto : zoneDto.getDivisionDistributions()) {
                DivisionDistribution divisionDist = new DivisionDistribution();
                divisionDist.setZoneDistribution(zoneDistribution);

                Integer divisionIdToFind= divisionDto.getDivisionId();
                String jpql = "SELECT z FROM ZoneDivisions z WHERE z.divisions.state_id = :divisionId AND z.zone.zoneId = :zoneId";
                ZoneDivisions division = entityManager.createQuery(jpql, ZoneDivisions.class)
                        .setParameter("divisionId", divisionDto.getDivisionId())
                        .setParameter("zoneId", zoneDistribution.getZone().getZoneId())
                        .getSingleResult();

                if (division == null) {
                    throw new IllegalArgumentException("Division not found with id: " + divisionDto.getDivisionId().intValue());
                }
                divisionDist.setDivisions(division);
                if(divisionDist.getMaleVacancy()!=null&&divisionDist.getFemaleVacancy()!=null&&divisionDist.getTotalVacancy()!=null) {
                    if (divisionDist.getMaleVacancy() + divisionDist.getFemaleVacancy() != divisionDist.getTotalVacancy())
                        throw new IllegalArgumentException("Male vacancies and female vacancies sum not equal to total for division distribution");

                    divisionDist.setMaleVacancy(divisionDto.getMaleVacancy());
                    divisionDist.setFemaleVacancy(divisionDto.getFemaleVacancy());
                    divisionDist.setTotalVacancy(divisionDist.getTotalVacancy());
                }
                divisionDist.setDivisionRunningField(divisionDto.getDivisionRunningField());
                divisionDist.setIsGenderWise(divisionDto.getIsGenderWise());
                divisionDist.setAdditionalComment(divisionDto.getAdditionalComment());
                if (Boolean.TRUE.equals(divisionDto.getIsGenderWise())) {
                    divisionDist.setMaleVacancy(divisionDto.getMaleVacancy());
                    divisionDist.setFemaleVacancy(divisionDto.getFemaleVacancy());
                    divisionDist.setTotalVacancy(divisionDto.getMaleVacancy() + divisionDto.getFemaleVacancy());
                } else if (divisionDto.getCategoryDistributions() != null && !divisionDto.getCategoryDistributions().isEmpty()) {
                    Integer totalVacancy = divisionDto.getCategoryDistributions().stream()
                            .mapToInt(DivisionCategoryDistributionDto::getVacancyCount)
                            .sum();
                    divisionDist.setTotalVacancy(totalVacancy);
                } else {
                    divisionDist.setTotalVacancy(divisionDto.getTotalVacancy());
                }

                // Persist the division distribution first
                entityManager.persist(divisionDist);

                // Save category distributions if present
                if (divisionDto.getCategoryDistributions() != null) {
                    List<DivisionCategoryDistribution> categoryDistributions = saveDivisionCategoryDistributions(
                            divisionDto.getCategoryDistributions(),
                            divisionDist
                    );
                    divisionDist.setCategoryDistributions(categoryDistributions);
                }

                divisionDistributions.add(divisionDist);
            }
            zoneDistribution.setDivisionDistributions(divisionDistributions);
            zoneDistribution.setAdditionalComments(zoneDistribution.getAdditionalComments());
        } catch (IllegalArgumentException e) {
            exceptionHandlingService.handleException(e);
            throw new IllegalArgumentException(e.getMessage());
        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            throw new Exception(e.getMessage());
        }
    }

    private List<DivisionCategoryDistribution> saveDivisionCategoryDistributions(
            List<DivisionCategoryDistributionDto> dtos,
            DivisionDistribution divisionDist
    ) {
        List<DivisionCategoryDistribution> categoryDistributions = new ArrayList<>();

        for (DivisionCategoryDistributionDto dto : dtos) {
            DivisionCategoryDistribution categoryDist = new DivisionCategoryDistribution();
            categoryDist.setDivisionDistribution(divisionDist);

            CustomReserveCategory category = entityManager.find(CustomReserveCategory.class, dto.getCategoryId());
            if (category == null) {
                throw new IllegalArgumentException("Category not found with id: " + dto.getCategoryId());
            }
            if(divisionDist.getIsGenderWise()) {
                if (dto.getMaleVacancy() + dto.getFemaleVacancy() != dto.getTotalVacancy())
                    throw new IllegalArgumentException("Vacancy for men and woman should be equal to total for cateogry id " + dto.getCategoryId());
            }
            if(dto.getCategoryId()!=6&&dto.getCategoryRunningField()!=null)
            {
                throw new IllegalArgumentException("Cannot add running field for category except OTHERS");
            }
            else if(dto.getCategoryId()==6&&(dto.getCategoryRunningField()==null||dto.getCategoryRunningField().trim().isEmpty()))
            {
                throw new IllegalArgumentException("Running field required for category when selecting OTHERS");
            }
            categoryDist.setCategoryRunningField(dto.getCategoryRunningField());
            categoryDist.setCategory(category);
            categoryDist.setVacancyCount(dto.getVacancyCount());
            categoryDist.setAdditionalComment(dto.getAdditionalComment());
            categoryDist.setMaleVacancy(dto.getMaleVacancy());
            categoryDist.setFemaleVacancy(dto.getFemaleVacancy());
            categoryDist.setTotalVacancy(dto.getTotalVacancy());
            entityManager.persist(categoryDist);
            categoryDistributions.add(categoryDist);
        }

        return categoryDistributions;
    }
    private void updatePostGenderDistribution(PostDto postDto, Post post) {
        System.out.println("reached update gender");
        GenderDistributionDto genderDto = postDto.getGenderWiseDistribution();
        if (genderDto == null) {
            return;
        }
        List<Integer>distributionTypeIds= postDto.getVacancyDistributionTypeIds();
        if(distributionTypeIds==null )
        {
        genderDto.setCategoryDistributionDtos(null);
        }
        if(distributionTypeIds.isEmpty())
        {
            genderDto.setCategoryDistributionDtos(null);
        }

        // Get or create gender distribution
        GenderWiseDistribution genderDist = post.getGenderWiseDistribution();
        boolean isNewDistribution = false;

        if (genderDist == null) {
            genderDist = new GenderWiseDistribution();
            genderDist.setPost(post);
            isNewDistribution = true;
        }

        // Update basic fields
        genderDist.setIsGenderWise(genderDto.getIsGenderWise());
        genderDist.setAdditionalComments(genderDto.getAdditionalComments());
        if (Boolean.TRUE.equals(genderDto.getIsGenderWise())) {
            genderDist.setMaleVacancy(genderDto.getMaleVacancy());
            genderDist.setFemaleVacancy(genderDto.getFemaleVacancy());
            genderDist.setTotalVacancy(genderDto.getMaleVacancy() + genderDto.getFemaleVacancy());
        } else {
            genderDist.setMaleVacancy(null);
            genderDist.setFemaleVacancy(null);
            genderDist.setTotalVacancy(genderDto.getTotalVacancy());
        }

        // Only persist if it's a new distribution
        if (isNewDistribution) {
            entityManager.persist(genderDist);
        }

        // Update category distributions if present
        List<CategoryDistributionDto> categoryDtos = genderDto.getCategoryDistributionDtos();
        if (categoryDtos != null && !categoryDtos.isEmpty()) {
            // Clear existing distributions
            if (genderDist.getCategoryDistributions() != null) {
                // Remove existing category distributions
                List<CategoryDistribution> existingDistributions = genderDist.getCategoryDistributions();
                for (CategoryDistribution existing : existingDistributions) {
                    entityManager.remove(existing);
                }
                existingDistributions.clear();
                entityManager.flush();
            }


            // Create new category distributions
            List<CategoryDistribution> newDistributions = new ArrayList<>();
            for (CategoryDistributionDto catDto : categoryDtos) {
                CategoryDistribution catDist = new CategoryDistribution();
                catDist.setGenderWiseDistribution(genderDist);

                if(catDto.getIsStateLevelCategory().equals(true))
                {
                    catDist.setIsStateLevelCategory(catDto.getIsStateLevelCategory());

                    catDist.setStateLevelCategory(catDto.getStateLevelCategory());
                    StateCode stateCode= entityManager.find(StateCode.class,catDto.getStateId());
                    if(stateCode==null)
                    {
                        throw new IllegalArgumentException("State with id "+ catDto.getStateId()+ " does not exist");
                    }
                    catDist.setState(stateCode);
                    catDist.setCategory(null);

                }
                else {
                    CustomReserveCategory category = entityManager.find(CustomReserveCategory.class, catDto.getCategoryId());
                    if (category == null) {
                        throw new IllegalArgumentException("Category not found with id: " + catDto.getCategoryId());
                    }
                    catDist.setIsStateLevelCategory(false);
                    catDist.setCategory(category);
                }
                if(catDto.getIsGenderWise()) {
                    if(catDto.getMaleVacancy()+catDto.getFemaleVacancy()!=catDto.getVacancyCount())
                        throw new IllegalArgumentException("Total vacancy should be equal to sum of male and female in category distribution");
                    catDist.setMaleVacancy(catDto.getMaleVacancy());
                    catDist.setFemaleVacancy(catDto.getFemaleVacancy());
                    catDist.setTotalVacancy(catDist.getTotalVacancy());
                }
                if(catDto.getCategoryId()!=null)
                {
                    if(catDto.getCategoryId()!=6&&catDto.getCategoryRunningField()!=null)
                        throw new IllegalArgumentException("Cannot add running field for category except OTHERS");
                    if(catDto.getCategoryId()==6&&(catDto.getCategoryRunningField()==null||catDto.getCategoryRunningField().trim().isEmpty()))
                        throw new IllegalArgumentException("Running field required when selecting category : OTHERS");
                }
                catDist.setCategoryRunningField(catDto.getCategoryRunningField());
                catDist.setVacancyCount(catDto.getVacancyCount());

                entityManager.persist(catDist);
                newDistributions.add(catDist);
            }

            Long totalCategoryVacancies=0L;
            for(CategoryDistribution categoryDistribution: newDistributions)
            {
                totalCategoryVacancies+=categoryDistribution.getVacancyCount();
            }
            genderDist.setTotalVacancy(totalCategoryVacancies);
            genderDist.setCategoryDistributions(newDistributions);
        }

        // Set the gender distribution back to post without merging
        post.setGenderWiseDistribution(genderDist);
    }
}