
package com.community.api.services;
import javax.persistence.EntityManager;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

import static com.community.api.services.ProductService.calculateDateRange;

@Service
public class PostService {

    private final EntityManager entityManager;
    private final ExceptionHandlingService exceptionHandlingService;
    private final ReserveCategoryService reserveCategoryService;
    private final GenderService genderService;
    private final QualificationDetailsService qualificationDetailsService;
    protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private ProductReserveCategoryBornBeforeAfterRefService reserveCategoryBornBeforeAfterRefService;
    @Autowired
    private ProductReserveCategoryBornBeforeAfterRefService productReserveCategoryBornBeforeAfterRefService;

    public PostService(EntityManager entityManager, ExceptionHandlingService exceptionHandlingService,ReserveCategoryService reserveCategoryService,GenderService genderService,QualificationDetailsService qualificationDetailsService) {
        this.entityManager = entityManager;
        this.exceptionHandlingService = exceptionHandlingService;
        this.reserveCategoryService = reserveCategoryService;
        this.genderService=genderService;
        this.qualificationDetailsService=qualificationDetailsService;
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
            if (addProductDto.getReserveCategoryAge()==null) {
                throw new IllegalArgumentException("Reserve category cannot be empty.");
            }
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
                else
                {
                    addProductAgeDTO.setAsOfDate(null);
                    addProductAgeDTO.setMaxAge(0);
                    addProductAgeDTO.setMinAge(0);
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
                    if (addProductAgeDTO.getBornBefore().after(maxBornBeforeDate)) {
                        throw new IllegalArgumentException("Born before date must be at least 5 years in the past.");
                    }
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
    private Post savePost(PostDto postDto,Product product) throws Exception {
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
        //  persisting the post once, regardless of distribution types
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
        entityManager.flush();// Ensure Post is saved and has an ID
        for (QualificationEligibilityDto qualificationEligibilityDto:postDto.getQualificationEligibility()) {
            if (qualificationEligibilityDto != null) {
                if (qualificationEligibilityDto.getQualificationIds() != null) {
                    QualificationEligibility qualificationRequirement = new QualificationEligibility();
                    //set qualifications
                    List<Integer> qualificationIds = qualificationEligibilityDto.getQualificationIds();
                    List<Qualification> qualificationsToAdd = new ArrayList<>();
                    if (qualificationIds != null) {
                        for (Integer qualificationId : qualificationIds) {
                            Qualification qualification = entityManager.find(Qualification.class, qualificationId);
                            qualificationsToAdd.add(qualification);
                        }
                        qualificationRequirement.setQualifications(qualificationsToAdd);
                    }

                    //set subjects
                    List<Long> subjectIds = qualificationEligibilityDto.getCustomSubjectIds();
                    if (subjectIds != null) {
                        if (!subjectIds.isEmpty()) {
                            List<CustomSubject> subjectsToAdd = new ArrayList<>();
                            for (Long subjectId : subjectIds) {
                                CustomSubject customSubject = entityManager.find(CustomSubject.class, subjectId);
                                subjectsToAdd.add(customSubject);
                            }
                            qualificationRequirement.setCustomSubjects(subjectsToAdd);
                        }
                    }

                    //set streams
                    List<Long> streamIds = qualificationEligibilityDto.getCustomStreamIds();
                    List<CustomStream> streamsToAdd = new ArrayList<>();
                    if (streamIds != null) {
                        for (Long streamId : streamIds) {
                            CustomStream customStream = entityManager.find(CustomStream.class, streamId);
                            streamsToAdd.add(customStream);
                        }
                        qualificationRequirement.setCustomStreams(streamsToAdd);

                    }
                    if (qualificationEligibilityDto.getCustomReserveCategoryId() != null) {
                        CustomReserveCategory customReserveCategory = entityManager.find(CustomReserveCategory.class, qualificationEligibilityDto.getCustomReserveCategoryId());
                        qualificationRequirement.setCustomReserveCategory(customReserveCategory);
                    }
                    qualificationRequirement.setPercentage(qualificationEligibilityDto.getPercentage());
                    qualificationRequirement.setPost(post);
                    qualificationRequirement.setIsPercentage(qualificationEligibilityDto.getIsPercentage());
                    qualificationRequirement.setCgpa(qualificationEligibilityDto.getCgpa());
                    entityManager.persist(qualificationRequirement);
                }
                entityManager.flush();
            }
        }
        List<AddPhysicalRequirementDto> physicalRequirementDtos = postDto.getPhysicalRequirements();
        if (!physicalRequirementDtos.isEmpty()) {
            for (AddPhysicalRequirementDto dto : physicalRequirementDtos) {
                CustomGender customGender = genderService.getGenderByGenderId(dto.getGenderId());
                if (customGender == null) {
                    throw new IllegalArgumentException("Gender not found for ID: " + dto.getGenderId());
                }

                CustomProductGenderPhysicalRequirementRef requirement = new CustomProductGenderPhysicalRequirementRef();
                requirement.setCustomGender(customGender);
                requirement.setHeight(dto.getHeight());
                requirement.setWeight(dto.getWeight());
                requirement.setShoeSize(dto.getShoeSize());
                requirement.setWaistSize(dto.getWaistSize());
                requirement.setChestSize(dto.getChestSize());
                requirement.setPost(post);

                entityManager.persist(requirement);
            }
            entityManager.flush();
        }

        entityManager.refresh(post);
        if (postDto.getVacancyDistributionTypeIds() != null && postDto.getVacancyDistributionTypeIds().contains(4)) {
            if (postDto.getOtherDistributions() != null && !postDto.getOtherDistributions().isEmpty()) {
                List<OtherDistribution> otherDistributions = saveOtherDistributions(postDto.getOtherDistributions(), post);
                post.setOtherDistributions(otherDistributions);
            }
        }

        return post;
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

            stateDistribution.setIsDistrictDistribution(stateDto.getIsDistrictDistribution());

            if (Boolean.TRUE.equals(stateDto.getIsDistrictDistribution())) {
                // Calculate total state vacancies from districts
                Integer totalStateVacancies = calculateDistrictBasedStateVacancies(stateDto);
                stateDistribution.setTotalVacanciesInState(totalStateVacancies);

                entityManager.persist(stateDistribution);
                saveDistrictDistributions(stateDto, stateDistribution);
            } else {
                // Calculate and set state level vacancies
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
                if (category == null) {
                    throw new IllegalArgumentException("Category not found with id: " + catDto.getCategoryId());
                }
                catDist.setCategory(category);
                catDist.setCategoryVacancies(catDto.getCategoryVacancies());
                totalVacancies += catDto.getCategoryVacancies();

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
            categoryDist.setCategory(category);
            categoryDist.setVacancyCount(dto.getVacancyCount());

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

            if (Boolean.TRUE.equals(zoneDto.getIsDivisionDistribution())) {
                // Calculate total vacancies in the zone based on divisions
                Integer totalZoneVacancies = calculateDivisionBasedZoneVacancies(zoneDto);
                zoneDistribution.setTotalVacanciesInZone(totalZoneVacancies);

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
                catDist.setCategoryVacancies(catDto.getCategoryVacancies());
                totalVacancies += catDto.getCategoryVacancies();

                entityManager.persist(catDist);
                categoryDistributions.add(catDist);
            }

            // Explicitly set the category distributions list
            zoneDistribution.setCategoryDistributions(categoryDistributions);

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

                divisionDist.setIsGenderWise(divisionDto.getIsGenderWise());

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
            categoryDist.setCategory(category);
            categoryDist.setVacancyCount(dto.getVacancyCount());

            entityManager.persist(categoryDist);
            categoryDistributions.add(categoryDist);
        }

        return categoryDistributions;
    }
    private void updatePostGenderDistribution(PostDto postDto, Post post) {

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

                CustomReserveCategory category = entityManager.find(CustomReserveCategory.class, catDto.getCategoryId());
                if (category == null) {
                    throw new IllegalArgumentException("Category not found with id: " + catDto.getCategoryId());
                }

                catDist.setCategory(category);
                catDist.setCategoryVacancies(catDto.getCategoryVacancies());

                entityManager.persist(catDist);
                newDistributions.add(catDist);
            }

            Long totalCategoryVacancies=0L;
            for(CategoryDistribution categoryDistribution: newDistributions)
            {
                totalCategoryVacancies+=categoryDistribution.getCategoryVacancies();
            }
            genderDist.setTotalVacancy(totalCategoryVacancies);
            genderDist.setCategoryDistributions(newDistributions);
        }

        // Set the gender distribution back to post without merging
        post.setGenderWiseDistribution(genderDist);
    }
}