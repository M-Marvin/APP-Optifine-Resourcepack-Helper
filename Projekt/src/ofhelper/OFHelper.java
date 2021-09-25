package ofhelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OFHelper {
	
	public static void main(String... args) {
		
		List<File> loadFiles = new ArrayList<File>();
		File listFile = null;
		File shapeFile = null;
		File tempFolder = null;
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-addTexture")) {
				File file = new File(args[i + 1]);
				if (file.exists() && !file.isDirectory()) {
					loadFiles.add(file);
					System.out.println("File detected: " + file);
					i++;
				}
			} else if (args[i].equals("-setTempFolder")) {
				File file = new File(args[i + 1]);
				if (file.exists() && file.isDirectory()) {
					tempFolder = file;
					System.out.println("TENP_FOLDER set to " + file);
					i++;
				}
			} else if (args[i].equals("-setShapeMode")) {
				File file = new File(args[i + 1]);
				if (file.exists() && !file.isDirectory()) {
					shapeFile = file;
					System.out.println("Shape File detected: " + file);
					i++;
				}
			} else if (args[i].equals("-setListMode")) {
				File file = new File(args[i + 1]);
				if (file.exists() && !file.isDirectory()) {
					listFile = file;
					System.out.println("List File detected: " + file);
					i++;
				}
			}
		}
		
		boolean validTexture = shapeFile != null && loadFiles.size() > 0;
		boolean validList = listFile != null;
		
		if (validList && tempFolder != null) {
			
			System.out.println("Start CTM File Mode");
			FileGen.makeFiles(listFile, tempFolder);
			
		} else if (validTexture && tempFolder != null) {
			
			System.out.println("Start Shape Textures Mode");
			TextureGen.makeTexturesForBlocks(shapeFile, loadFiles.toArray(new File[] {}), tempFolder);
			
		} else {
			
			System.err.println("Missing Files or Folders!");
			System.exit(-1);
			
		}
		
	}
	
}
