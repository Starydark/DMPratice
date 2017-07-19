package JSONFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ReadFile {
	
	public static String ReadFile(String path) throws IOException {
		File file = new File(path);
		
		if(!file.exists() || file.isDirectory()){
			throw new FileNotFoundException();
		}
		
		StringBuffer sb = new StringBuffer();
		try (FileReader fileReader = new FileReader(file)){
			//InputStreamReader read = new InputStreamReader(new FileInputStream(file), "UTF-8");
			try(BufferedReader bufferdReader = new BufferedReader(fileReader)){
				String lineTxt = null;
				while ((lineTxt = bufferdReader.readLine()) != null) {
					sb.append(lineTxt);
				}
			} catch (Exception e) {
			e.printStackTrace();
			}
		}
		return sb.toString();
	}
}
