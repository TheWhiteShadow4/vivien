
// Dateiendungen, für die es eine Preview gibt.
// Muss mit der Liste in tws.vivien.core.PreviewModule im Backend übereinstimmen.
export const SUPPORTED_PREVIEW_TYPES = [ "toml", "png", "jpg", "jpeg", "tif", "tiff", "tga", "txt", "md", "json", "yaml", "xml", "html" ];

export function getFileExtension(path: string): string | null
{
	const dotIndex = path.lastIndexOf('.');
	if (dotIndex === -1) return null;
	return path.slice(dotIndex + 1).toLowerCase();
}

export const README_FILE = {
	name: "Vivien.md",
	path: "Vivien.md",
	type: "FILE"
};

export const SETUP_FILE = {
	name: "setup.toml",
	path: "setup.toml",
	type: "FILE"
};