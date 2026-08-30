package tws.vivien.handlers;

import tws.vivien.core.Cache;
import tws.vivien.core.Repository;
import tws.vivien.dto.FileObject;

import java.nio.file.Path;

public interface IHandler
{
	FileObject generatePreview(Path webRoot, Repository repository, Cache cache, String file) throws Exception;
}
