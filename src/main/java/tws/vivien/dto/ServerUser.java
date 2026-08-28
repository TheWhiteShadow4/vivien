package tws.vivien.dto;

/// Benutzer, mit dem sich der Benutzer authentifiziert hat.
/// Nicht zwingend der Benutzer selbst.
/// @see UserSettings
public record ServerUser(String name) {}
