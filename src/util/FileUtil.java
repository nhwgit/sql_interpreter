package util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class FileUtil {

	public static <T> T readObjectFromFile(T obj, String fileName) {
		try(ObjectInputStream oi =
				new ObjectInputStream(new FileInputStream(fileName))) {
				obj = (T)oi.readObject();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
			catch(ClassNotFoundException e) {
				e.printStackTrace();
			}
		return obj;
	}

	public static <T> void writeObjectToFile(T obj, String fileName) {
		try(ObjectOutputStream oo = new ObjectOutputStream(
				new FileOutputStream(fileName))) {
			oo.writeObject(obj);
		}
		catch(IOException e) { e.printStackTrace(); }
	}

	public static <T> void deleteFile(Path fileName) {
		try {
			Files.delete(fileName);
		} catch(NoSuchFileException e) {
			System.out.println("삭제하려는 파일이 없습니다.");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
