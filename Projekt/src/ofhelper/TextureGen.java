package ofhelper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class TextureGen {

	public static final int[][] TEXTURE_COMBINE_MAP = new int[][] {
		new int[] {1, 3, -1, -1, 0}, 
		new int[] {3, -1, -1, -1, 1}, 
		new int[] {0, 3, -1, -1, 0}, 
		new int[] {1, 3, -1, -1, 1}, 
		new int[] {0, 3, -1, -1, 1}, 
		new int[] {0, 1, 3, -1, 1}, 
		new int[] {0, 3, 2, -1, 1}, 
		new int[] {1, -1, -1, -1, 1}, 
		new int[] {0, 1, 2, 3, 1},
		new int[] {0, -1, -1, -1, 1}, 
		new int[] {1, 2, -1, -1, 1}, 
		new int[] {0, 2, -1, -1, 1}, 
		new int[] {1, 2, 3, -1, 1}, 
		new int[] {0, 1, 2, -1, 1}, 
		new int[] {1, 2, -1, -1, 0}, 
		new int[] {2, -1, -1, -1, 1}, 
		new int[] {0, 2, -1, -1, 0}
	};

	File shapeFile = null;
	List<File> textureFiles = new ArrayList<File>();
	
	/**
	 * Generate all needed Optifine overlay textures for all blocks in the list.
	 * Clears the tempFolder before it makes the next block.
	 * 
	 * @param shapeFile The 4 basic Square overlays
	 * @param textureFile The block texture list
	 */
	public static void makeTexturesForBlocks(File shapeFile, File[] textureFiles, File tempFolder) {
		
		File resultFolder = new File(tempFolder.getParent(), "/Output/");
		if (!resultFolder.exists() || !resultFolder.isDirectory()) resultFolder.mkdir();
		
		for (File textureFile : textureFiles) {
			
			File outputFolder = makeTextures(shapeFile, textureFile, tempFolder);
			
			System.out.println("Copy files in output folder ...");
			File[] files = outputFolder.listFiles();
			String folderName = outputFolder.getName();
			File targetFolder = new File(resultFolder, "/ctm/" + folderName + "/");
			targetFolder.mkdir();
			
			for (File path : files) {
				
				String fileName = path.getName();
				File newPath = new File(targetFolder, fileName);
				try {
					Files.move(path.toPath(), newPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					System.err.println("Cant move texture " + path);
					e.printStackTrace();
					System.exit(-1);
				}
				
			}
			
			System.out.println("Files copyed, deleting temp folder ...");
			deleteDir(tempFolder);
			tempFolder.mkdirs();
			
			System.out.println("Complete!");
			
		}
		
	}

	/**
	 * Generate all needed Optifine overlay textures for 1 block.
	 * 
	 * @param shapeFile The 4 basic Square overlays
	 * @param textureFile The block texture
	 * @return The path of the folder with all textures
	 */
	public static File makeTextures(File shapeFile, File textureFile, File tempFolder) {
		
		System.out.println("Start Generating Textures...");
		
		System.out.println("Load Textures in Cache ...");
		BufferedImage combinedShapes = readImmage(shapeFile);
		BufferedImage textureToCopy = readImmage(textureFile);
		
		System.out.println("Cut combined shape textures ...");
		BufferedImage[] shapeImages = cutShapeImage(combinedShapes);
		
		int i = 0;
		for (BufferedImage image : shapeImages) {
			i++;
			writeTexture(image, new File(tempFolder, "/temp" + i + ".png"));
		}
		System.out.println("4 temp images saved");
		
		BufferedImage debug = combineTextures(shapeImages[1], shapeImages[3], true);
		writeTexture(debug, new File(tempFolder, "/debug.png"));
		
		System.out.println("Generate overlap textures ...");
		BufferedImage[] shapeOverlays = generateShapeImages(shapeImages);
		
		System.out.println("Transfer overlays to texture " + textureFile.getName());
		BufferedImage[] finalTextures = transferOverlays(textureToCopy, shapeOverlays);
		
		String blockName = textureFile.getName().split("\\.")[0];
		
		System.out.println("Create output folder \"" + blockName + "\"");
		File outputFolder = new File(tempFolder, "/" + blockName + "/");
		outputFolder.mkdir();
		
		System.out.println("Save final textures ...");
		i = 0;
		for (BufferedImage image : finalTextures) {
			writeTexture(image, new File(outputFolder, i + ".png"));
			i++;
		}
		System.out.println(finalTextures.length + " textures saved");
		
		System.out.println("Block " + blockName + " complete!");
		return outputFolder;
		
	}
	
	/**
	 * Copys the overlyas in the list to the block texture.
	 * 
	 * @param texture Block texture
	 * @param shapeOverlays Overlays
	 * @return A list with the overlap-textures for optifine
	 */
	public static BufferedImage[] transferOverlays(BufferedImage texture, BufferedImage[] shapeOverlays) {
		
		BufferedImage[] finalTextures = new BufferedImage[shapeOverlays.length];
		
		for (int i = 0; i < shapeOverlays.length; i++) {
			
			BufferedImage overlay = shapeOverlays[i];
			finalTextures[i] = combineTextures(texture, overlay, true);
			
		}
		
		return finalTextures;
		
	}
	
	/**
	 * Read a texture from a file.
	 * 
	 * @param File Path to the image file
	 * @return The texture in the file
	 */
	public static BufferedImage readImmage(File file) {
		
		try {
			
			InputStream is = new FileInputStream(file);
			BufferedImage bufferedImage = ImageIO.read(is);
			is.close();
			
			return bufferedImage;
			
		} catch (IOException e) {
			System.err.println("Cant read immage file: " + file);
			e.printStackTrace();
			System.exit(-1);
			return null;
		}
		
	}
	
	/**
	 * Save a texture in a file.
	 * 
	 * @param image Image to save.
	 * @param file Path to save.
	 */
	public static void writeTexture(BufferedImage image, File file) {
		
		try {
			
			file.createNewFile();
			ImageIO.write(image, "png", file);
			
		} catch (IOException e) {
			System.err.println("Cant write immage file: " + file);
			e.printStackTrace();
			System.exit(-1);
		}
		
	}
	
	/**
	 * Cuts the (normaly 32x32) cobined Shape Image in 4 Images.
	 * Order in CombinedTexture:
	 *  Top Left: Left
	 *  Top Right: Right
	 *  Bottom Left: Up
	 *  Bottom Right: Down
	 * Order in Array: Left Right Up Down
	 * 
	 * @param combinedShapes The (normaly 32x32) combined Shape Image
	 * @return An array of the Shape Images
	 */
	public static BufferedImage[] cutShapeImage(BufferedImage combinedShapes) {
		
		int resolution = Math.min(combinedShapes.getWidth(), combinedShapes.getHeight()) / 2;
		
		int[] pixels_left = combinedShapes.getRGB(0, 0, resolution, resolution, null, 0, resolution);
		int[] pixels_right = combinedShapes.getRGB(resolution, 0, resolution, resolution, null, 0, resolution);
		int[] pixels_up = combinedShapes.getRGB(0, resolution, resolution, resolution, null, 0, resolution);
		int[] pixels_down = combinedShapes.getRGB(resolution, resolution, resolution, resolution, null, 0, resolution);
		
		BufferedImage shapeLeft = new BufferedImage(resolution, resolution, BufferedImage.TYPE_INT_ARGB);
		shapeLeft.setRGB(0, 0, resolution, resolution, pixels_left, 0, resolution);
		BufferedImage shapeRight= new BufferedImage(resolution, resolution, BufferedImage.TYPE_INT_ARGB);
		shapeRight.setRGB(0, 0, resolution, resolution, pixels_right, 0, resolution);
		BufferedImage shapeUp = new BufferedImage(resolution, resolution, BufferedImage.TYPE_INT_ARGB);
		shapeUp.setRGB(0, 0, resolution, resolution, pixels_up, 0, resolution);
		BufferedImage shapeDown = new BufferedImage(resolution, resolution, BufferedImage.TYPE_INT_ARGB);
		shapeDown.setRGB(0, 0, resolution, resolution, pixels_down, 0, resolution);
		
		BufferedImage[] shapeImages = new BufferedImage[] {shapeLeft, shapeRight, shapeUp, shapeDown};
		return shapeImages;
		
	}
	
	/**
	 * Generate all overlap-shapes for Optifine (17 Overlays).
	 * Uses the static TEXTURE_COMBINE_MAP
	 * 
	 * @param shapeImages A list with the 4 basic overlays (4 Square sides)
	 * @return A sorted list with all overlays.
	 */
	public static BufferedImage[] generateShapeImages(BufferedImage[] shapeImages) {
		
		BufferedImage[] shapes = new BufferedImage[TEXTURE_COMBINE_MAP.length];
		
		for (int i = 0; i < TEXTURE_COMBINE_MAP.length; i++) {
			
			int shape0 = TEXTURE_COMBINE_MAP[i][0];
			int copy1 = TEXTURE_COMBINE_MAP[i][1];
			int copy2 = TEXTURE_COMBINE_MAP[i][2];
			int copy3 = TEXTURE_COMBINE_MAP[i][3];
			boolean invert = TEXTURE_COMBINE_MAP[i][4] == 0;
			
			BufferedImage shape = shapeImages[shape0];
			if (copy1 >= 0) shape = combineTextures(shape, shapeImages[copy1], invert);
			if (copy2 >= 0) shape = combineTextures(shape, shapeImages[copy2], invert);
			if (copy3 >= 0) shape = combineTextures(shape, shapeImages[copy3], invert);
			
			shapes[i] = shape;
			
		}
		
		return shapes;
		
	}
	
	/**
	 * Copys pixels from shape2 to shape1.
	 * When not inverted, only copy alpha > 127 Pixels.
	 * When inverted, only copys slpha < 127 Pixels.
	 * 
	 * @param shape1 Image to combinde with shape2
	 * @param shape2 Image to copy out pixels
	 * @param invert2 invert alpha filter
	 * @return shape1 copy with filtered pixels from shape2
	 */
	public static BufferedImage combineTextures(BufferedImage shape1, BufferedImage shape2, boolean invert2) {

		int resolution = Math.min(shape1.getWidth(), shape1.getHeight());
		
		BufferedImage result = copyImage(shape1);
		
		for (int x = 0; x < resolution; x++) {
			for (int y = 0; y < resolution; y++) {
				
				int rgb = shape2.getRGB(x, y);
				
				int alpha = (rgb >> 24) & 0xFF;
				
				if (invert2 == alpha < 127) {
					
					result.setRGB(x, y, rgb);
					
				}
				
			}
		}
		
		return result;
		
	}
	
	/**
	 * Create a copy of an image.
	 * 
	 * @param image Image to copy
	 * @return Copy of the Image.
	 */
	public static BufferedImage copyImage(BufferedImage image) {
		int resolution = Math.min(image.getWidth(), image.getHeight());
		BufferedImage image2 = new BufferedImage(resolution, resolution, BufferedImage.TYPE_INT_ARGB);
		int[] pixels = image.getRGB(0, 0, resolution, resolution, null, 0, resolution);
		image2.setRGB(0, 0, resolution, resolution, pixels, 0, resolution);
		return image2;
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
