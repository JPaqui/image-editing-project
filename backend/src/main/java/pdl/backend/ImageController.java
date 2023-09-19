package pdl.backend;

import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.madgag.gif.fmsware.GifDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


@RestController
public class ImageController {

    private final ImageDao imageDao;
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    public ImageController(ImageDao imageDao) {
        this.imageDao = imageDao;
    }

    // Returns the image with the corresponding id to the client
    @RequestMapping(value = "/images/{id}", method = RequestMethod.GET, produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_GIF_VALUE})
    public ResponseEntity<?> getImage(@PathVariable("id") long id) {
        Optional<Image> img = imageDao.retrieve(id);
        if (img.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        byte[] bytes = img.get().getData();
        return ResponseEntity.ok().contentType(img.get().mediaType).body(bytes);
    }

    // Delete the image with the corresponding id form imageDao
    @RequestMapping(value = "/images/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteImage(@PathVariable("id") long id) {
        Optional<Image> img = imageDao.retrieve(id);
        if (img.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        imageDao.delete(img.get());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // Add the image stored in file to imageDao
    @RequestMapping(value = "/images", method = RequestMethod.POST)
    public ResponseEntity<?> addImage(@RequestParam("file") MultipartFile file, @RequestParam(name = "fileName", required = false) String name) {
        try {
            MediaType mediaType = MediaType.parseMediaType(Objects.requireNonNull(file.getContentType()));
            if (!(mediaType.equals(MediaType.IMAGE_JPEG) || mediaType.equals(MediaType.IMAGE_PNG) || mediaType.equals(MediaType.IMAGE_GIF))) {
                return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            }
            if (name == null) name = file.getOriginalFilename();
            imageDao.create(new Image(name, file.getBytes(), mediaType));
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    //Apply the algorithm in params to the posted image and send it back
    @RequestMapping(value = "/images", method = RequestMethod.POST, produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE}, params = {"algorithm"})
    public ResponseEntity<?> modifyPostedImage(@RequestParam("file") MultipartFile file, @RequestParam Map<String, String> params) {
        try {
            MediaType mediaType = MediaType.parseMediaType(Objects.requireNonNull(file.getContentType()));
            if (!(mediaType.equals(MediaType.IMAGE_JPEG) || mediaType.equals(MediaType.IMAGE_PNG) || mediaType.equals(MediaType.IMAGE_GIF))) {
                return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            }
            byte[] bytes = executeProgram(file.getBytes(), mediaType, params);
            return ResponseEntity.ok().contentType(mediaType).body(bytes);
        } catch (IOException | NullPointerException e) {
            System.out.println(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Returns a JSON variable containing information from all the image in imageDao
    @RequestMapping(value = "/images", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    @ResponseBody
    public ArrayNode getImageList() throws IOException {
        ArrayNode nodes = mapper.createArrayNode();
        for (Image image : imageDao.retrieveAll()) {
            ObjectNode node = mapper.createObjectNode();
            node.put("id", image.getId());
            node.put("name", image.getName());
            node.put("type", image.mediaType.toString());
            try (ByteArrayInputStream is = new ByteArrayInputStream(image.getData())) {
                BufferedImage bImg = ImageIO.read(is);
                Planar<GrayU8> planar = ConvertBufferedImage.convertFromPlanar(bImg, null, true, GrayU8.class);
                node.put("size", String.format("%d*%d*%d", planar.width, planar.height, planar.getNumBands()));
                nodes.add(node);
            }
        }
        return nodes;
    }

    // Returns the image with the corresponding id after it have been modified by the algorithm
    @RequestMapping(value = {"/images/{id}"}, method = RequestMethod.GET, produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_GIF_VALUE}, params = {"algorithm"})
    public ResponseEntity<?> getModifiedImage(@PathVariable("id") long id, @RequestParam HashMap<String, String> params) {
        Optional<Image> img = imageDao.retrieve(id);
        if (img.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        try {
            byte[] bytes = executeProgram(img.get().getData(), img.get().mediaType, params);
            return ResponseEntity.ok().contentType(img.get().mediaType).body(bytes);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NumberFormatException | ImageControllerException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // DEBUG ONLY : Print the parameters to the console
    void printParams(Map<String, String> params) {
        for (String key : params.keySet()) {
            System.out.println("param : " + key + ", value : " + params.get(key));
        }
    }

    // Returns a byte array of img after treatment by the algorithm
    byte[] executeProgram(byte[] bytes, MediaType mediaType, Map<String, String> params) throws IOException, ImageControllerException {
        InputStream is = new ByteArrayInputStream(bytes);
        BufferedImage bImg;
        Planar<GrayU8> input;
        Planar<GrayU8> output;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            if (mediaType.equals(MediaType.IMAGE_GIF)) {
                GifDecoder gif = new GifDecoder();
                gif.read(is);
                AnimatedGifEncoder giff = new AnimatedGifEncoder();
                giff.setRepeat(0);
                giff.start(baos);
                for (int i = 0; i < gif.getFrameCount(); i++) {
                    bImg = gif.getFrame(i);
                    giff.setDelay(gif.getDelay(i));
                    input = ConvertBufferedImage.convertFromPlanar(bImg, null, true, GrayU8.class);
                    input.reorderBands(1, 2, 3, 0);
                    output = input.createSameShape();
                    if (ImageModifier.treatInput(input, output, params)) {
                        output.reorderBands(3, 0, 1, 2);
                        bImg = new BufferedImage(output.width, output.height, bImg.getType());
                        ConvertBufferedImage.convertTo(output, bImg, true);
                    } else {
                        input.reorderBands(3, 0, 1, 2);
                        ConvertBufferedImage.convertTo(input, bImg, true);
                    }
                    giff.addFrame(bImg);
                }
            } else {
                bImg = ImageIO.read(is);
                input = ConvertBufferedImage.convertFromPlanar(bImg, null, true, GrayU8.class);
                output = input.createSameShape();
                if (ImageModifier.treatInput(input, output, params)) {
                    bImg = new BufferedImage(output.width, output.height, bImg.getType());
                    ConvertBufferedImage.convertTo(output, bImg, true);
                } else
                    ConvertBufferedImage.convertTo(input, bImg, true);
                if (mediaType.equals(MediaType.IMAGE_JPEG))
                    ImageIO.write(bImg, "jpeg", baos);
                else if (mediaType.equals(MediaType.IMAGE_PNG))
                    ImageIO.write(bImg, "png", baos);
            }

            bytes = baos.toByteArray();
            return bytes;
        } catch (IOException e) {
            throw new IOException("Could not convert image");
        } catch (NumberFormatException e) {
            throw new ImageControllerException("Illegal argument format");
        }
    }
}
