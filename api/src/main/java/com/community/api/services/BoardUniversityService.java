package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.BoardUnviDTO;
import com.community.api.entity.BoardUniversity;
import com.community.api.entity.BoardUniversityLocation;
import com.community.api.entity.BoardUniversityType;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class BoardUniversityService
{
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandlingService;
    @Autowired
    private BoardUniversityService boardUniversityService;
    @Autowired
    private ResponseService responseService;
    @Autowired
    private JwtUtil jwtTokenUtil;
    @Autowired
    RoleService roleService;

    @Transactional
    public BoardUniversity addBoardUniversities(BoardUnviDTO boardUniversity, String authHeader) {
        String jwtToken = authHeader.substring(7);

        Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);

        String role = roleService.getRoleByRoleId(roleId).getRole_name();
        List<BoardUniversity> savedBoardUniversities = new ArrayList<>();

            BoardUniversity boardUniversityToBeSaved =new BoardUniversity();
            long id = findCount() + 1;
            if (boardUniversity.getBoard_university_name() == null || boardUniversity.getBoard_university_name().trim().isEmpty()) {
                throw new IllegalArgumentException("Board or University name cannot be empty or consist only of whitespace");
            }
            if (boardUniversity.getBoard_university_code() == null || boardUniversity.getBoard_university_code().trim().isEmpty()) {
                throw new IllegalArgumentException("Board or University code cannot be empty or consist only of whitespace");
            }
            if (boardUniversity.getBoard_university_type() == null) {
                throw new IllegalArgumentException("Board or University type cannot be empty or consist only of whitespace");
            }
            if (boardUniversity.getBoard_university_location() == null || boardUniversity.getBoard_university_location().trim().isEmpty()) {
                throw new IllegalArgumentException("Board or University location cannot be empty or consist only of whitespace");
            }
            if (!boardUniversity.getBoard_university_location().matches("^[#a-zA-Z0-9].*")) {
                throw new IllegalArgumentException("Board or University location must start with #, letter, or number");
            }

            if (boardUniversity.getBoard_university_location().matches(".*[~`!@$%^*\\\\|;<>?].*")) {
                throw new IllegalArgumentException("Board or University location contains invalid special characters");
            }

            if (boardUniversity.getBoard_university_location().matches("^[()_\\-{}\\[\\]/\":&,. \n]+$")) {
                throw new IllegalArgumentException("Board or University location cannot contain only special characters");
            }
            if (boardUniversity.getBoard_university_location().matches("^[0-9]+$")) {
                throw new IllegalArgumentException("Board or University location cannot contain only numbers");
            }

            if (!boardUniversity.getBoard_university_name().matches("^[a-zA-Z][a-zA-Z ]*$")) {
                throw new IllegalArgumentException("Board or University name cannot contain numeric values, special characters, or leading spaces");
            }
            if (!boardUniversity.getBoard_university_code().matches("^[a-zA-Z][a-zA-Z ]*$")) {
                throw new IllegalArgumentException("Board or university code cannot contain numeric values, special characters, or leading spaces");
            }
            if (!boardUniversity.getBoard_university_type().matches("^[a-zA-Z][a-zA-Z ]*$")) {
                throw new IllegalArgumentException("Board or university type cannot contain numeric values, special characters, or leading spaces");
            }
            if(!boardUniversity.getBoard_university_type().equalsIgnoreCase("BOARD") && !boardUniversity.getBoard_university_type().equalsIgnoreCase("UNIVERSITY"))
            {
                throw new IllegalArgumentException("Board or university type can be either 'BOARD' or 'UNIVERSITY'");
            }
            List<BoardUniversity> boardUniversities = getAllBoardUniversities();
            for (BoardUniversity existingBoardUniversity : boardUniversities) {
                if (existingBoardUniversity.getBoard_university_name().equalsIgnoreCase(boardUniversity.getBoard_university_name())) {
                    throw new IllegalArgumentException("Duplicate name not allowed");
                }
                if (existingBoardUniversity.getBoard_university_code().equalsIgnoreCase(boardUniversity.getBoard_university_code())) {
                    throw new IllegalArgumentException("Duplicate code not allowed");
                }
            }
        Query query = entityManager.createQuery("SELECT MAX(i.sortOrder) FROM BoardUniversity i WHERE i.sortOrder <> 1000000");
        Long sortOrder = (Long) query.getSingleResult();
            boardUniversityToBeSaved.setBoard_university_name(boardUniversity.getBoard_university_name());
            boardUniversityToBeSaved.setSortOrder(sortOrder);

            boardUniversityToBeSaved.setBoard_university_code(boardUniversity.getBoard_university_code());

            boardUniversityToBeSaved.setCreated_by(role);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String now = LocalDateTime.now().format(formatter);
            boardUniversityToBeSaved.setArchived(false);
            boardUniversityToBeSaved.setCreated_date(now);
            entityManager.persist(boardUniversityToBeSaved);
        BoardUniversityLocation location=new BoardUniversityLocation();
        location.setBoardUniversityId(boardUniversityToBeSaved.getBoard_university_id());
        location.setBoard_university_location(boardUniversity.getBoard_university_location());
        BoardUniversityType type=new BoardUniversityType();
        type.setBoardUniversityId(boardUniversityToBeSaved.getBoard_university_id());
        type.setBoard_university_type(boardUniversity.getBoard_university_type());
        entityManager.persist(location);
        entityManager.persist(type);
            savedBoardUniversities.add(boardUniversityToBeSaved);
            return boardUniversityToBeSaved;
    }

    public List<BoardUniversity> getAllBoardUniversities() {
        TypedQuery<BoardUniversity> query = entityManager.createQuery(
                Constant.FIND_ALL_BOARD_UNIVERSITY_QUERY, BoardUniversity.class);
        query.setParameter("archived", false);
        List<BoardUniversity> boardUniversityList = query.getResultList(); // then execute
        return boardUniversityList;
    }


    //need to be change here
    public long findCount() {
        String queryString = Constant.GET_BOARD_UNIVERSITY_COUNT;
        TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
        return query.getSingleResult();
    }

    @Transactional
    public BoardUniversity updateBoardUniversity(Long boardUniversityId, BoardUnviDTO boardUniversity,String authHeader){
        String jwtToken = authHeader.substring(7);

        Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);

        String role = roleService.getRoleByRoleId(roleId).getRole_name();
        BoardUniversity boardUniversityToUpdate= entityManager.find(BoardUniversity.class,boardUniversityId);
        if(boardUniversityToUpdate==null)
        {
            throw new IllegalArgumentException("Board or University with id "+ boardUniversityId+" not found");
        }
        List<BoardUniversity> boardUniversities = getAllBoardUniversities();
        if (Objects.nonNull(boardUniversity.getBoard_university_name())) {
            if (!boardUniversity.getBoard_university_name().matches("^[a-zA-Z][a-zA-Z ]*$")) {
                throw new IllegalArgumentException("Board or University name cannot contain numeric values, special characters or leading spaces");
            }
            for (BoardUniversity existingBoardUniversity : boardUniversities) {
                if (existingBoardUniversity.getBoard_university_name().equalsIgnoreCase(boardUniversity.getBoard_university_name()) && !existingBoardUniversity.getBoard_university_id().equals(boardUniversityId)) {
                    throw new IllegalArgumentException("Duplicate name not allowed");
                }
            }
            boardUniversityToUpdate.setBoard_university_name(boardUniversity.getBoard_university_name());
        }
        if (Objects.nonNull(boardUniversity.getBoard_university_code())) {
            if (!boardUniversity.getBoard_university_code().matches("^[a-zA-Z][a-zA-Z ]*$")){
                throw new IllegalArgumentException("Board or university code cannot contain numeric values, special characters or leading spaces");
            }
            for (BoardUniversity existingBoardUniversity : boardUniversities) {
                if (existingBoardUniversity.getBoard_university_code().equalsIgnoreCase(boardUniversity.getBoard_university_code()) && !existingBoardUniversity.getBoard_university_id().equals(boardUniversityId)) {
                    throw new IllegalArgumentException("Duplicate code not allowed");
                }
            }
            boardUniversityToUpdate.setBoard_university_code(boardUniversity.getBoard_university_code());
        }
        BoardUniversityLocation boardUniversityLocation=entityManager.find(BoardUniversityLocation.class,boardUniversityId);
        BoardUniversityType boardUniversityType=entityManager.find(BoardUniversityType.class,boardUniversityId);
        if (Objects.nonNull(boardUniversity.getBoard_university_type())) {
            if (!boardUniversity.getBoard_university_type().matches("^[a-zA-Z][a-zA-Z ]*$")){
                throw new IllegalArgumentException("Board or university type cannot contain numeric values ,special characters or leading spaces");
            }
            if(!boardUniversity.getBoard_university_type().equalsIgnoreCase("BOARD") && !boardUniversity.getBoard_university_type().equalsIgnoreCase("UNIVERSITY"))
            {
                throw new IllegalArgumentException("Board or university type can be either 'BOARD' or 'UNIVERSITY'");
            }
            boardUniversityType.setBoard_university_type(boardUniversity.getBoard_university_type().toUpperCase());
        }
        if (Objects.nonNull(boardUniversity.getBoard_university_location())) {
            if (!boardUniversity.getBoard_university_location().matches("^[#a-zA-Z0-9].*")) {
                throw new IllegalArgumentException("Board or University location must start with #, letter, or number");
            }

            if (boardUniversity.getBoard_university_location().matches(".*[~`!@$%^*\\\\|;<>?].*")) {
                throw new IllegalArgumentException("Board or University location contains invalid special characters");
            }

            if (boardUniversity.getBoard_university_location().matches("^[()_\\-{}\\[\\]/\":&,. \n]+$")) {
                throw new IllegalArgumentException("Board or University location cannot contain only special characters");
            }
            if (boardUniversity.getBoard_university_location().matches("^[0-9]+$")) {
                throw new IllegalArgumentException("Board or University location cannot contain only numbers");
            }
            boardUniversityLocation.setBoard_university_location(boardUniversity.getBoard_university_location());
        }
        if(boardUniversity.getCreated_date()!=null|| boardUniversity.getCreated_by()!=null)
        {
            throw new IllegalArgumentException("Created Date and Created By cannot be modified");
        }
        boardUniversityToUpdate.setModified_by(role);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(formatter);
        boardUniversityToUpdate.setModified_date(now);
        entityManager.merge(boardUniversityLocation);
        entityManager.merge(boardUniversityType);
        return entityManager.merge(boardUniversityToUpdate);
    }
    @Transactional
    public BoardUniversity manageUni(Long boardUnivId, Boolean active) throws Exception {
        try {
            BoardUniversity subject = entityManager.find(BoardUniversity.class, boardUnivId);
            if (subject == null) {
                throw new IllegalArgumentException("Subject not found");
            }

            if (active) {
                if (subject.getArchived()) {
                    throw new IllegalArgumentException("Subject already archived");
                }
                subject.setArchived(true);
            } else {
                if (!subject.getArchived()) {
                    throw new IllegalArgumentException("Subject already unarchived");
                }
                subject.setArchived(false);
            }

            entityManager.merge(subject);
            return subject;
        } catch (Exception e) {
            throw e;
        }
    }
}
