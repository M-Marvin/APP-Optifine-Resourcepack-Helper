package ofhelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class FileGen {
	
	File shapeFile = null;
	List<File> textureFiles = new ArrayList<File>();
	
	public static void makeFiles(File fileList, File tempFolder) {
		
		File resultFolder = new File(tempFolder.getParent(), "/Output/");
		
		try {
			
			System.out.println("Start reading list ...");
			InputStream is = new FileInputStream(fileList);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line;
			List<String> lines = new ArrayList<String>();
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
			reader.close();
			
			if (lines.size() > 0) {
				
				for (int i = 0; i < lines.size(); i++) {
					
					String blockEntry = lines.get(i);
					char flag = blockEntry.charAt(0);
					
					System.out.println("Create files for entry " + blockEntry + " ...");
					
					List<String> blocksBelow = new ArrayList<String>();
					for (int i1 = i + 1; i1 < lines.size(); i1++) {
						blocksBelow.add(lines.get(i1));
					}
					
					System.out.println("Write file (if needed) ...");
					makeFile(blockEntry, blocksBelow, resultFolder, flag);
					
				}
				
			}
			
			System.out.println("Complete!");
			
		} catch (FileNotFoundException e) {
			System.err.println("File " + fileList.getPath() + " not found!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void makeFile(String blockEntry, List<String> blocksBelow, File tempFolder, char flag) {
		
		if (flag == '-') return;
		
		String blockName = isValidFlag(flag) ? blockEntry.substring(1) : blockEntry;
		
		List<String> ignoredBlocks = new ArrayList<String>();
		if (blockEntry.contains("[")) {
			String removeBlocks = blockEntry.split("\\[")[1].split("\\]")[0];
			for (String remove : removeBlocks.split(",")) {
				ignoredBlocks.add(remove);
			}
			blockName = blockName.split("\\[")[0];
		}
		if (blockEntry.contains("(")) {
			String addBlocks = blockEntry.split("\\(")[1].split("\\)")[0];
			for (String add : addBlocks.split(",")) {
				blocksBelow.add(add);
			}
			blockName = blockName.split("\\(")[0];
		}
		
		StringBuilder fileBuilder = new StringBuilder();
		
		fileBuilder.append("matchBlocks=");
		for (int i = 0; i < blocksBelow.size(); i++) {
			String block = blocksBelow.get(i);
			char flag1 = block.charAt(0);
			String lineEntry = isValidFlag(flag1) ? block.substring(1) : block;
			lineEntry = lineEntry.split("\\[")[0].split("\\(")[0];
			if (ignoredBlocks.contains(lineEntry)) continue;
			fileBuilder.append(lineEntry + (i != blocksBelow.size() - 1 ? " " : ""));
		}
		
		fileBuilder.append("\nmethod=overlay\ntiles=0-16\n");
		
		fileBuilder.append((flag == '*' ? "connectTiles=" : "connectBlock=") + blockName);
		
		File ctmFolder = new File(tempFolder, "/ctm/" + blockName + "/" + blockName + ".properties");
		
		try {
			ctmFolder.getParentFile().mkdirs();
			ctmFolder.createNewFile();
			OutputStream os = new FileOutputStream(ctmFolder);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
			writer.write(fileBuilder.toString());
			writer.close();
		} catch (IOException e) {
			System.err.println("ERROR on writing file " + ctmFolder.getPath() + "!");
		}
		
	}
	
	public static boolean isValidFlag(char flag) {
		return flag == '*' || flag == '~' || flag == '-' || flag == '-';
	}
	
	public static void deleteDir(File dir) {

		File[] files = dir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDir(files[i]);
				}
				else {
					files[i].delete();
				}
			}
			dir.delete();
		}
	}
	
}
