package io.swagger.swaggerhub.plugin.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.models.Info;
import io.swagger.models.Swagger;
import io.swagger.swaggerhub.plugin.exceptions.DefinitionParsingException;
import io.swagger.util.Yaml;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

public class DefinitionParserServiceTest {

    private static DefinitionParserService definitionParserService;
    private Swagger swagger;
    private ObjectMapper objectMapper;

    private static final String SAMPLE_DEFINITION_TITLE = "Sample Definition Title";

    @Before
    public void setupTestClass(){
        definitionParserService = new DefinitionParserService();

        this.swagger = new Swagger();
        Info info = new Info();
        swagger.setInfo(info);

        objectMapper = new ObjectMapper();
    }

    @Test
    public void validDefinition_canParseApiTitleWithSpacesTest() throws Exception {
        //Given
        swagger.getInfo().setTitle("Sample API Title");
        String swaggerString = objectMapper.writeValueAsString(swagger);

        //When
        String apiId = definitionParserService.getApiId(Yaml.mapper().readTree(swaggerString));

        //Then
        assertEquals("Sample_API_Title", apiId);

    }

    @Test
    public void validDefinition_canParseApiTitleWithSpecialCharsTest() throws Exception {
        //Given
        swagger.getInfo().setTitle("Sample!API%Title**");
        String swaggerString = objectMapper.writeValueAsString(swagger);

        //When
        String apiId = definitionParserService.getApiId(Yaml.mapper().readTree(swaggerString));

        //Then
        assertEquals("Sample_API_Title", apiId);

    }

    @Test(expected = DefinitionParsingException.class)
    public void definition_missingTitle_throwsExceptionWhenParsingForApiIdTest() throws Exception {
        //Given
        String swaggerString = objectMapper.writeValueAsString(swagger);
        JsonNode swaggerNode = Yaml.mapper().readTree(swaggerString);
        swaggerNode = removeElementFromNode(swaggerNode.get("info"), "title");

        //When
        definitionParserService.getApiId(swaggerNode);

        //Then
        fail();
    }

    @Test(expected = DefinitionParsingException.class)
    public void definition_missingInfoSection_throwsExceptionWhenParsingForApiIdTest() throws Exception {
        //Given
        String swaggerString = objectMapper.writeValueAsString(swagger);
        JsonNode swaggerNode = Yaml.mapper().readTree(swaggerString);
        swaggerNode = removeElementFromNode(swaggerNode, "info");

        //When
        definitionParserService.getApiId(swaggerNode);

        //Then
        fail();
    }

    @Test
    public void validDefinition_canParseVersionTest() throws Exception {
        //Given
        String apiVersion = "1.0.0";
        swagger.getInfo().setVersion(apiVersion);
        String swaggerString = objectMapper.writeValueAsString(swagger);

        //When
        String parsedApiVersion = definitionParserService.getVersion(Yaml.mapper().readTree(swaggerString));

        //Then
        assertEquals(apiVersion, parsedApiVersion);

    }

    @Test(expected = DefinitionParsingException.class)
    public void definition_missingVersion_throwsExceptionWhenParsingForVersionTest() throws Exception {
        //Given
        String swaggerString = objectMapper.writeValueAsString(swagger);
        JsonNode swaggerNode = Yaml.mapper().readTree(swaggerString);
        swaggerNode = removeElementFromNode(swaggerNode.get("info"), "version");

        //When
        definitionParserService.getVersion(swaggerNode);

        //Then
        fail();
    }

    @Test(expected = DefinitionParsingException.class)
    public void definition_missingInfoSection_throwsExceptionWhenParsingForVersionTest() throws Exception {
        //Given
        String swaggerString = objectMapper.writeValueAsString(swagger);
        JsonNode swaggerNode = Yaml.mapper().readTree(swaggerString);
        swaggerNode = removeElementFromNode(swaggerNode, "info");

        //When
        definitionParserService.getVersion(swaggerNode);

        //Then
        fail();
    }

    @Test
    public void validJSONDefinition_canBeConvertedToJsonNode() throws DefinitionParsingException {
        //Given
        String validJson = String.format("{" +
                " \"info\":{" +
                "    \"title\": \"%s\" }" +
                " }", SAMPLE_DEFINITION_TITLE);

        //When
        JsonNode definitionJsonNode = definitionParserService.convertDefinitionToJsonNode(validJson, DefinitionFileFormat.JSON);

        //sThen
        assertEquals(SAMPLE_DEFINITION_TITLE, definitionJsonNode.get("info").get("title").textValue());
    }

    @Test
    public void validYamlDefinition_canBeConvertedToJsonNode() throws DefinitionParsingException {
        //Given
        String validYaml = String.format("info:\n" +
                "   title: %s", SAMPLE_DEFINITION_TITLE);

        //When
        JsonNode definitionJsonNode = definitionParserService.convertDefinitionToJsonNode(validYaml, DefinitionFileFormat.YAML);

        //sThen
        assertEquals(SAMPLE_DEFINITION_TITLE, definitionJsonNode.get("info").get("title").textValue());
    }

    @Test(expected = DefinitionParsingException.class)
    public void invalidJsonDefinition_throwsException_whenConvertingToJsonNode() throws DefinitionParsingException {
        //Given
        String invalidJson = "{ \"info\":       {title:@@£$!}}";

        //When
        definitionParserService.convertDefinitionToJsonNode(invalidJson, DefinitionFileFormat.JSON);

        //Then
        fail();
    }

    @Test(expected = DefinitionParsingException.class)
    public void invalidYamlDefinition_throwsException_whenConvertingToJsonNode() throws DefinitionParsingException {
        //Given
        String invalidYaml = "\"info\":    version 1.0.0   title: Sample Definition Title}";

        //When
        definitionParserService.convertDefinitionToJsonNode(invalidYaml, DefinitionFileFormat.YAML);

        //Then
        fail();
    }

    private JsonNode removeElementFromNode(JsonNode node, String fieldName){
        return ((ObjectNode) node).remove(fieldName);
    }
}
