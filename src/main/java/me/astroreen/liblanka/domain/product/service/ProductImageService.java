package me.astroreen.liblanka.domain.product.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luciad.imageio.webp.WebPWriteParam;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.astroreen.liblanka.domain.product.entity.Product;
import me.astroreen.liblanka.domain.product.entity.ProductColor;
import me.astroreen.liblanka.domain.product.entity.ProductImage;
import me.astroreen.liblanka.domain.product.repository.ProductImageRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductColorService productColorService;
    private final ProductImageRepository productImageRepository;
    private static final Logger log = LoggerFactory.getLogger(ProductImageService.class);

    @Transactional
    public Product saveAllImages(@NotNull Product originalProduct, @NotNull List<MultipartFile> images)
            throws IOException {
        log.debug("Saving {} images for product ID {}", images.size(), originalProduct.getId());

        List<ProductImage> productImages = new ArrayList<>();
        for (MultipartFile image : images) {
            if (image.isEmpty()) {
                log.warn("Skipping empty image file: {}", image.getOriginalFilename());
                continue;
            }

            byte[] imageData = image.getBytes();
            if (imageData.length == 0) {
                log.warn("Image file {} has no data", image.getOriginalFilename());
                continue;
            }

            // Convert to webp
            byte[] webpData = convertToWebp(imageData);

            ProductImage productImage = ProductImage.builder()
                    .product(originalProduct)
                    .imageData(webpData)
                    .color(null)
                    .build();

            productImages.add(productImage);
        }

        if (productImages.isEmpty()) {
            log.warn("No valid images to save for product ID {}", originalProduct.getId());
            return originalProduct;
        }

        productImageRepository.saveAll(productImages);
        originalProduct.setImages(productImages);
        log.debug("Saved {} images for product ID {}", productImages.size(), originalProduct.getId());
        return originalProduct;
    }

    @Transactional
    public Product saveImagesWithColorData(@NotNull Product originalProduct, @NotNull List<MultipartFile> images, String jsonColorIds)
            throws IllegalArgumentException, NoSuchElementException, IOException {
        log.debug("Saving {} images with color IDs for product ID {}", images.size(), originalProduct.getId());
        ObjectMapper objectMapper = new ObjectMapper();
        List<Long> colorIds = objectMapper.readValue(jsonColorIds, new TypeReference<List<Long>>() {});

        if (images.size() != colorIds.size()) {
            throw new IllegalArgumentException("The number of images must match the number of color IDs");
        }

        List<ProductColor> allColors = productColorService.findAll();
        Map<MultipartFile, ProductColor> data = new HashMap<>();
        for (int i = 0; i < images.size(); i++) {
            Long colorId = colorIds.get(i);
            ProductColor imageColor = null;
            if (colorId != null) {
                imageColor = allColors.stream()
                        .filter(productColor -> Objects.equals(productColor.getId(), colorId))
                        .findFirst()
                        .orElseThrow(() -> new NoSuchElementException("Color ID " + colorId + " could not be found in the database"));
            }
            data.put(images.get(i), imageColor);
        }

        List<ProductImage> productImages = new ArrayList<>();
        for (Map.Entry<MultipartFile, ProductColor> entry : data.entrySet()) {
            byte[] imageData = entry.getKey().getBytes();
            // Convert to webp
            byte[] webpData = convertToWebp(imageData);
            ProductImage productImage = ProductImage.builder()
                    .product(originalProduct)
                    .imageData(webpData)
                    .color(entry.getValue())
                    .build();
            productImages.add(productImage);
        }

        productImageRepository.saveAll(productImages);
        originalProduct.setImages(productImages);
        return originalProduct;
    }

    @Transactional
    public Product saveImageWithColor(@NotNull Product originalProduct, MultipartFile image, Long colorId)
        throws IOException {
        ProductColor color = productColorService.findAll().stream()
        .filter(productColor -> Objects.equals(productColor.getId(), colorId))
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("Color ID " + colorId + " could not be found in the database"));
        
        byte[] webpData = convertToWebp(image.getBytes());

        ProductImage coloredImage = ProductImage.builder()
            .product(originalProduct)
            .imageData(webpData)
            .color(color)
            .build();
        
        productImageRepository.save(coloredImage);
        List<ProductImage> images = originalProduct.getImages();
        images.add(coloredImage);
        originalProduct.setImages(images);
        return originalProduct;
    }

    @Transactional
    public byte[] getImageData(Long imageId) throws IllegalArgumentException {
        ProductImage productImage = productImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image with id " + imageId + " was not found"));
        return productImage.getImageData();
    }

    // Helper to convert image bytes to webp (80% quality)
    private byte[] convertToWebp(byte[] imageBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        BufferedImage inputImage = ImageIO.read(bais);
        if (inputImage == null) {
            throw new IOException("Unsupported image format for conversion to webp");
        }
        ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/webp").next();
        writer.setOutput(ios);

        WebPWriteParam param = new WebPWriteParam(writer.getLocale());
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType(param.getCompressionTypes()[WebPWriteParam.LOSSY_COMPRESSION]);
            param.setCompressionQuality(0.8f); // 80% quality
        }
        
        IIOImage iioImage = new IIOImage(inputImage, null, null);
        writer.write(null, iioImage, param);
        
        writer.dispose();
        ios.close();
        baos.close();

        return baos.toByteArray();
    }
}
