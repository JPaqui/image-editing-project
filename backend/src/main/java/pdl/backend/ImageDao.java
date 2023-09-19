package pdl.backend;

import org.apache.commons.io.FilenameUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.StreamSupport;

@Repository
public class ImageDao implements Dao<Image> {

    private final Map<Long, Image> images = new HashMap<>();

    public ImageDao() throws IOException {
        this("../");
    }

    public ImageDao(String imagesDirectoryPath) throws IOException {
        // Placez les images dans un dossier images dans le répertoire où le serveur est lancé.
        String imagesDirectoryName = imagesDirectoryPath + "/images";
        Path imagesPath = Paths.get(imagesDirectoryName);
        findImage(imagesPath);
    }

    public static MediaType getMediaTypeFromExtension(String fileExtension) {
        switch (fileExtension) {
            case "png":
                return MediaType.IMAGE_PNG;
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG;
            case "gif":
                return MediaType.IMAGE_GIF;
        }
        return null;
    }

    private void findImage(Path directoryPath) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directoryPath)) {
            for (Path filePath : stream) {
                File file = new File(filePath.toString());
                if (file.isDirectory()) {
                    findImage(filePath);
                } else {
                    MediaType mediaType = getMediaTypeFromExtension(FilenameUtils.getExtension(filePath.toString()));
                    if (mediaType != null) {
                        byte[] fileContent;
                        fileContent = Files.readAllBytes(filePath);
                        Image img = new Image(filePath.getFileName().toString(), fileContent, mediaType);
                        images.put(img.getId(), img);
                    }
                }
            }
        } catch (IOException e) {
            throw new NoSuchFileException("\"images\" folder not found");
        }
    }

    @Override
    public Optional<Image> retrieve(final long id) {
        return Optional.ofNullable(images.get(id));
    }

    @Override
    public List<Image> retrieveAll() {
        return new ArrayList<>(images.values());
    }

    @Override
    public void create(final Image img) {
        images.put(img.getId(), img);
    }

    @Override
    public void update(final Image img, final String[] params) {
        // Not used
    }

    @Override
    public void delete(final Image img) {
        images.remove(img.getId());
    }
}
