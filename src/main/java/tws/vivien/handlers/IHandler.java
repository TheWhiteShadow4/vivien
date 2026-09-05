package tws.vivien.handlers;

import tws.vivien.core.Cache;
import tws.vivien.core.Config;
import tws.vivien.core.Repository;
import tws.vivien.dto.FileObject;

public interface IHandler
{
	FileObject generatePreview(Config config, Repository repository, Cache cache, String file) throws Exception;
}
