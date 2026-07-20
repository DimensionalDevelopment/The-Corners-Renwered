package net.ludocrypt.corners;

import net.minecraft.nbt.NbtIo;

import java.io.IOException;
import java.nio.file.Path;

public class DeepNbtGzipRecovery {
    public static void main(String[] args) throws IOException {
        Path inputFile = Path.of("D:\\Git Repos\\The-Corners\\src\\main\\resources\\data\\corners\\structures\\nbt\\communal_corridors\\communal_corridors_decorated\\communal_corridors_decorated_7.nbt");

        var nbt = NbtIo.read(inputFile);

        System.out.println(nbt);
    }
}