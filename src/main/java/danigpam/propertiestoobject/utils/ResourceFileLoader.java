package danigpam.propertiestoobject.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResourceFileLoader {

    private static Logger logger = LogManager.getLogger();
    
	private String resourceFilePath;
	
	public ResourceFileLoader(String resourceFilePath) {
		super();
		this.resourceFilePath = resourceFilePath;
	}

	public String getContentsAsString() {
		String fileAsText = "";
		BufferedReader br;
		try {
			InputStream is = getAsInputStream();
			
			br = new BufferedReader(new InputStreamReader(is));
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    fileAsText = sb.toString();
		    br.close();
		} catch (Exception e) {
			logger.error(e);
		}
		return fileAsText;
	}
	
	public InputStream getAsInputStream() {
		InputStream fileAsInputStream = null;
		ClassLoader classLoader = getClass().getClassLoader();
		fileAsInputStream = classLoader.getResourceAsStream(resourceFilePath);
		return fileAsInputStream;
	}
}