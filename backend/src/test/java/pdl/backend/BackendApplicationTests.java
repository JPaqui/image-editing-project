package pdl.backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
class BackendApplicationTests {

	@Test
	@Order(1)
	void contextLoads() {
	}

	@Test
	@Order(2)
	public void initialiseServerWithoutImagesFile(){

		IOException thrown = Assertions.assertThrows(IOException.class, () -> {
			new ImageDao("./"); //recherche du dossier "images" dans le dossier backend
		});
		Assertions.assertEquals("\"images\" folder not found", thrown.getMessage());
	}
	
	@Test
	@Order(3)
	public void initialiseServerWithImagesFile(){
		try {
			ImageDao imageDao = new ImageDao("./src/test/java/pdl/backend/testImages");
			List<Image> images = imageDao.retrieveAll();
			ArrayList<String> imagesNames = new ArrayList<>();
			ArrayList<String> expectedImagesNames = new ArrayList<>(List.of("americat.jpg", "francecat.jpg", "nyancat.png", "download.jpeg", "test.jpg"));
			for(Image image : images){
				imagesNames.add(image.getName());
			}
			assertTrue(imagesNames.containsAll(expectedImagesNames));
			assertTrue(expectedImagesNames.containsAll(imagesNames));
		}
		catch(Exception e){
			fail("Unexpected : " + e);
		}
	}

	@Test
	@Order(4)
	public void testOrderedFindImage(){

	}
}
