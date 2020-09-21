package com.zhokhov.progimage.storage.repository;

import com.zhokhov.progimage.storage.http.IncorrectImageIdException;
import com.zhokhov.progimage.shared.ImageType;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Optional;

public class ImageId {

    private final String hex;
    private final ImageType type;

    public ImageId(@NonNull ImageType type, @NonNull String hex) {
        this.hex = hex;
        this.type = type;
    }

    public ImageType getType() {
        return type;
    }

    public String getHex() {
        return hex;
    }

    public String toString() {
        return type.getFormatNumber() + "-" + hex + type.getExtension();
    }

    public static ImageId parseOriginal(String imageId) {
        String formatNumberString = imageId.contains("-") ? imageId.substring(0, imageId.indexOf("-")) : null;

        if (formatNumberString == null) {
            throw new IncorrectImageIdException(imageId);
        }

        int formatNumber;

        try {
            formatNumber = Integer.parseUnsignedInt(formatNumberString);
        } catch (NumberFormatException e) {
            throw new IncorrectImageIdException(imageId);
        }

        Optional<ImageType> imageType = ImageType.findByFormatNumber(formatNumber);

        if (imageType.isEmpty()) {
            throw new IncorrectImageIdException(imageId);
        }

        String hex = imageId.substring(imageId.indexOf("-") + 1, imageId.indexOf("."));

        return new ImageId(imageType.get(), hex);
    }

}
