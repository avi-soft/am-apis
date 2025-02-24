package com.community.api.dto;

import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.stereotype.Component;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class FormDataArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(FormDataOnly.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        if (!ServletFileUpload.isMultipartContent(request)) {
            throw new IllegalArgumentException("Request must be multipart/form-data");
        }

        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            throw new IllegalArgumentException("Query parameters are not allowed. All data must be sent as form-data.");
        }

        Class<?> paramType = parameter.getParameterType();
        Object dto = paramType.getDeclaredConstructor().newInstance();

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, String[]> formData = multipartRequest.getParameterMap();

        // First validate required fields are present
        validateRequiredFields(formData);

        for (Field field : paramType.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();

            if (formData.containsKey(fieldName)) {
                String[] values = formData.get(fieldName);
                if (field.getType().equals(List.class)) {
                    List<Object> list = new ArrayList<>();
                    Class<?> genericType = (Class<?>) ((ParameterizedType) field.getGenericType())
                            .getActualTypeArguments()[0];

                    // Special validation for modes field
                    if (fieldName.equals("modes")) {
                        validateModes(values);
                        for (String value : values) {
                            try {
                                list.add(Integer.parseInt(value.trim()));
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException("Invalid mode value provided");
                            }
                        }
                    }
                    // Special validation for customerIds field
                    else if (fieldName.equals("customerIds")) {
                        validateCustomerIds(values);
                        for (String value : values) {
                            try {
                                list.add(Long.parseLong(value.trim()));
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException("Invalid customer ID provided");
                            }
                        }
                    }
                    // Handle other List fields
                    else {
                        for (String value : values) {
                            if (genericType.equals(Long.class)) {
                                list.add(Long.parseLong(value));
                            } else if (genericType.equals(Integer.class)) {
                                list.add(Integer.parseInt(value));
                            } else {
                                list.add(value);
                            }
                        }
                    }
                    field.set(dto, list);
                } else if (values.length > 0) {
                    field.set(dto, values[0]);
                }
            }
        }

        // Handle file uploads
        if (MultipartFile.class.isAssignableFrom(paramType)) {
            return multipartRequest.getFile(parameter.getParameterName());
        } else {
            try {
                Field filesField = paramType.getDeclaredField("files");
                if (filesField != null) {
                    filesField.setAccessible(true);
                    filesField.set(dto, new ArrayList<>(multipartRequest.getFiles("files")));
                }
            } catch (NoSuchFieldException e) {
                // Ignore if files field doesn't exist
            }
        }

        return dto;
    }

    private void validateRequiredFields(Map<String, String[]> formData) {
        if (!formData.containsKey("customerIds")) {
            throw new IllegalArgumentException("Customer IDs are required");
        }
        if (!formData.containsKey("modes")) {
            throw new IllegalArgumentException("Communication modes are required");
        }
    }

    private void validateModes(String[] values) {
        if (values == null || values.length == 0 || (values.length == 1 && values[0].trim().isEmpty())) {
            throw new IllegalArgumentException("You have to select at least one mode");
        }
        for (String value : values) {
            if (value.trim().isEmpty()) {
                throw new IllegalArgumentException("You have to select at least one mode");
            }
        }
    }

    private void validateCustomerIds(String[] values) {
        if (values == null || values.length == 0 || (values.length == 1 && values[0].trim().isEmpty())) {
            throw new IllegalArgumentException("You have to select at least one customer");
        }
        for (String value : values) {
            if (value.trim().isEmpty()) {
                throw new IllegalArgumentException("You have to select at least one customer");
            }
        }
    }
}