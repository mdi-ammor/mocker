package com.bpce.mock.service;

import com.bpce.mock.exception.InvalidTypeException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import static com.bpce.mock.tools.LambdaExceptionUtils.rethrowConsumer;

@Service
public class ResponseGeneratorSrv {

    public JsonNode generateNode(JsonNode root) throws InvalidTypeException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode generated = mapper.createObjectNode();
        Set<String> fieldsToRemove = new HashSet<>();
        root.fieldNames().forEachRemaining(rethrowConsumer(field -> {
            if(root.get(field).isValueNode()) {
                if (root.get(field).isTextual() && root.get(field).asText().startsWith("=")) {
                    String[] parameters = root.get(field).asText().substring(1).split(Pattern.quote("|"));
                    switch(parameters[0]) {
                        case "uuid": ((ObjectNode) generated).put(field, UUID.randomUUID().toString()); break;
                        case "string" : {
                            int length = parameters.length == 2 ? convertLength(parameters[1], 10) : 10;
                            ((ObjectNode) generated).put(field, RandomStringUtils.randomAlphabetic(length));
                            break;
                        }
                        case "int" : {
                            int length = parameters.length == 2 ? convertLength(parameters[1], 2) : 2;
                            int min = (int) Math.pow(10, length - 1);
                            ((ObjectNode) generated).put(field,  ThreadLocalRandom.current().nextInt(min, min * 10));
                            break;
                        }
                        case "float" : ((ObjectNode) generated).put(field, generateFloat()); break;
                        case "phone": ((ObjectNode) generated).put(field, "+6" + RandomStringUtils.randomNumeric(9)); break;
                        case "ip": ((ObjectNode) generated).put(field, generateIp()); break;
                        case "date": ((ObjectNode) generated).put(field, generateDate()); break;
                        case "array": {
                            int length = parameters.length == 3 ? convertLength(parameters[2], 5) : 5;
                            String fieldName = parameters[1];
                            JsonNode arrayType = root.get(fieldName);
                            if(Objects.isNull(arrayType)) throw new InvalidTypeException(field, fieldName);
                            ((ObjectNode) generated).putPOJO(field, generateArray(arrayType, length));
                            fieldsToRemove.add(fieldName);
                            break;
                        }
                        case "enum": {
                            String[] possibleValues = parameters[1].split(",");
                            possibleValues = Arrays.stream(possibleValues).map(String::trim).toArray(String[]::new);
                            int index = new Random().nextInt(possibleValues.length);
                            ((ObjectNode) generated).put(field, possibleValues[index]);
                            break;
                        }
                        default: throw new InvalidTypeException(field, parameters[0]);
                    }
                } else {
                    ((ObjectNode) generated).set(field, root.get(field));
                }
            } else {
                JsonNode subNode = mapper.createObjectNode();
                ((ObjectNode) generated).set(field, generateNode(root.get(field)));
            }
        }));
        //clean
        fieldsToRemove.forEach(field -> ((ObjectNode) generated).remove(field));
        // result
        return generated;
    }

    private ArrayNode generateArray(JsonNode contentNode, int length) throws InvalidTypeException {
        ArrayNode arrayNode = new ObjectMapper().createArrayNode();

        for (int i = 0; i<length; i++)
            arrayNode.add(generateNode(contentNode));

        return arrayNode;
    }
    private String generateIp() {
        Random r = new Random();
        return "192." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);
    }
    private String generateDate(){
        int day = createRandomIntBetween(1, 28);
        int month = createRandomIntBetween(1, 12);
        int year = createRandomIntBetween(2021, 2022);
        return LocalDate.of(year, month, day).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG));
    }
    private static int createRandomIntBetween(int start, int end) {
        return start + (int) Math.round(Math.random() * (end - start));
    }
    private float generateFloat(){
        DecimalFormat df2 = new DecimalFormat(".##");
        double diff = 4500.99 - 1000;
        double randomValue = 1000 + Math.random( ) * diff;
        return Float.parseFloat(df2.format(randomValue).replace(",", "."));
    }
    private int convertLength(String number, int defaultValue) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
