package danigpam.propertiestoobject;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import danigpam.propertiestoobject.domain.SampleConfig;
import danigpam.propertiestoobject.utils.PropertiesFileLoader;
import danigpam.propertiestoobject.utils.ResourceFileLoader;

@SpringBootTest
class PropertiesToObjectApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	public void validateSampleObject() {
		InputStream is = new ResourceFileLoader("sample.properties").getAsInputStream();
 		SampleConfig configObj = new PropertiesFileLoader().load(SampleConfig.class, is);

 		assertEquals("my text", configObj.getText());
 		assertEquals("list entry #1", configObj.getList().get(0));
 		assertEquals("list entry #2", configObj.getList().get(1));

 		assertEquals("sample text in list[0]", configObj.getDetails().get(0).getText());
 		assertTrue(configObj.getDetails().get(0).isBool());
 		assertEquals(null, configObj.getDetails().get(0).getDecimal());
 		
 		assertEquals("sample text in list[1]", configObj.getDetails().get(1).getText());
 		assertEquals(null,configObj.getDetails().get(1).isBool());
 		assertEquals(3.0, configObj.getDetails().get(1).getDecimal());
 		
 		assertEquals("sample text in key1", configObj.getMap().get("key1").getText());
 		assertTrue(configObj.getMap().get("key1").isBool());
 		assertEquals(5.4, configObj.getMap().get("key1").getDecimal());

 		assertEquals("sample text in key2", configObj.getMap().get("key2").getText());
 		assertFalse(configObj.getMap().get("key2").isBool());
 		assertEquals(1.2, configObj.getMap().get("key2").getDecimal());
	}
}
