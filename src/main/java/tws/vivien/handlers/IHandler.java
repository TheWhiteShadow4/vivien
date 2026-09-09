package tws.vivien.handlers;

import tws.vivien.dto.FileObject;

public interface IHandler
{
	FileObject generatePreview(String file) throws Exception;
}
