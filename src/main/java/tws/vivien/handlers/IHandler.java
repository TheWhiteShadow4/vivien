package tws.vivien.handlers;

import tws.vivien.dto.FileObject;

import java.nio.file.Path;

public interface IHandler
{
	FileObject generatePreview(String file, Path path) throws Exception;
}
