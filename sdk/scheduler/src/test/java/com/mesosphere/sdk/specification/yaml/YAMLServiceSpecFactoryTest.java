package com.mesosphere.sdk.specification.yaml;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.mesosphere.sdk.offer.CommonTaskUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.FileUtils;
import com.mesosphere.sdk.specification.DefaultServiceSpec;
import static com.mesosphere.sdk.specification.yaml.YAMLServiceSpecFactory.*;
import static org.mockito.Mockito.when;

import com.mesosphere.sdk.testutils.OfferRequirementTestUtils;
import com.mesosphere.sdk.testutils.TestConstants;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class YAMLServiceSpecFactoryTest {
    @Rule public final EnvironmentVariables environmentVariables = OfferRequirementTestUtils.getApiPortEnvironment();
    @Mock private FileReader mockFileReader;

    @Before
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGenerateSpecFromYAML() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("valid-exhaustive.yml").getFile());

        when(mockFileReader.read("config-one.conf.mustache")).thenReturn("hello");
        when(mockFileReader.read("config-two.xml.mustache")).thenReturn("hey");
        when(mockFileReader.read("config-three.conf.mustache")).thenReturn("hi");

        DefaultServiceSpec serviceSpec = generateServiceSpec(generateRawSpecFromYAML(file), mockFileReader);
        Assert.assertNotNull(serviceSpec);
        Assert.assertEquals(TestConstants.PORT_API_VALUE, Integer.valueOf(serviceSpec.getApiPort()));
    }

    @Test
    public void testMustache() throws Exception {
        final Map<String, String> env = new HashMap<String, String>() {{
            put("DISKS", "PATH,/mnt/data_1,data-1,1000;PATH,/mnt/data_2,data-2,1000;PATH,/mnt/data_3,data-3,1000");
            put("PARSE_DISKS_INPUT_VAR", "DISKS");
            put("PARSE_DISKS_OUTPUT_VAR", "outputDisks");
        }};
        Writer w = new StringWriter();
        MustacheFactory mf = new DefaultMustacheFactory();
        String template = "{{#ParseDisks}}{{/ParseDisks}}\n" +
                    "{{#outputDisks}}\n" +
                    "{{Path}}: \n" +
                    "  type: {{Type}}\n" +
                    "  path: {{Path}}\n" +
                    "  root: {{Root}}\n" +
                    "  size: {{Size}}\n" +
                    "{{/outputDisks}}";

        Writer w2 = CommonTaskUtils.executeMustache(w, template, env);
        System.out.println(w2.toString());
    }

    @Test
    public void testGenerateRawSpecFromYAMLFile() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("valid-exhaustive.yml").getFile());
        RawServiceSpec rawServiceSpec = generateRawSpecFromYAML(file);
        Assert.assertNotNull(rawServiceSpec);
        Assert.assertEquals(TestConstants.PORT_API_VALUE, rawServiceSpec.getScheduler().getApiPort());
    }

    @Test
    public void testGenerateRawSpecFromYAMLString() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("valid-exhaustive.yml").getFile());
        String yaml = FileUtils.readFileToString(file);
        RawServiceSpec rawServiceSpec = generateRawSpecFromYAML(yaml);
        Assert.assertNotNull(rawServiceSpec);
        Assert.assertEquals(TestConstants.PORT_API_VALUE, rawServiceSpec.getScheduler().getApiPort());
    }
}
