
#define MyAppName "Vivien"
#define MyAppVersion "0.2"
#define MyAppPublisher "TheWhiteShadow"
#define JAVA_VERSION 25
#define JAVA_URL "https://download.java.net/openjdk/jdk25/ri/openjdk-25+36_windows-x64_bin.zip"

[Setup]
; NOTE: The value of AppId uniquely identifies this application. Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={{33F18627-6E3A-403C-B4FA-64541CDADB29}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={autopf}\{#MyAppName}
DefaultGroupName={#MyAppName}
DisableProgramGroupPage=yes
PrivilegesRequired=lowest
OutputBaseFilename=Vivien_Setup
SolidCompression=yes
WizardStyle=modern dynamic
SetupIconFile=icon.ico

[Languages]
Name: "german"; MessagesFile: "compiler:Languages\German.isl"

[Files]
Source: "..\target\{#MyAppName}-{#MyAppVersion}.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "vivien-server.toml"; DestDir: "{app}"; Flags: ignoreversion
Source: "7za.exe"; DestDir: "{tmp}"; Flags: deleteafterinstall
Source: "icon.ico"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
; Erstellt eine Desktop-Verknüpfung, die die JAR direkt über Java startet
Name: "{autodesktop}\{#MyAppName}"; Filename: "java.exe"; \
	Parameters: "-jar ""{app}\{#MyAppName}-{#MyAppVersion}.jar"""; WorkingDir: "{app}"; IconFilename: "{app}\icon.ico"

[Registry]
; Schreibt in den PATH des aktuellen Benutzers
Root: HKCU; Subkey: "Environment"; \
    ValueType: expandsz; ValueName: "Path"; ValueData: "{olddata};{app}\jdk-{#JAVA_VERSION}\bin"; \
    Check: ShouldAddPath

	
[UninstallDelete]
Type: filesandordirs; Name: "{app}\jdk-{#JAVA_VERSION}"
Type: filesandordirs; Name: "{app}\cache"

[Code]
var
  ConfigPage: TInputQueryWizardPage;
  DownloadPage: TDownloadWizardPage;
  JavaIsMissing: Boolean;


{ --- HILFSFUNKTION FÜR STRING-REPLACE --- }
procedure ReplaceTextInFile(const FileName, SearchStr, ReplaceStr: String);
var
  FileLines: TArrayOfString;
  I: Integer;
  LineText: String;
begin
  if FileExists(FileName) then
  begin
    { Lädt die gesamte Datei zeilenweise in ein Array }
    if LoadStringsFromFile(FileName, FileLines) then
    begin
      for I := 0 to GetArrayLength(FileLines) - 1 do
      begin
        { Zwischenspeichern der Zeile in einer lokalen Variable }
        LineText := FileLines[I];
        
        { StringChangeEx verändert 'LineText' direkt an Ort und Stelle }
        StringChangeEx(LineText, SearchStr, ReplaceStr, True);
        
        { Die modifizierte Zeile zurück in das Array schreiben }
        FileLines[I] := LineText;
      end;
      { Speichert die geänderte Datei wieder ab }
      SaveStringsToFile(FileName, FileLines, False);
    end;
  end;
end;

function OnDownloadProgress(const Url, FileName: String; const Progress, ProgressMax: Int64): Boolean;
begin
  Result := True;
  DownloadPage.SetProgress(Progress, ProgressMax);
end;


procedure InitializeWizard;
begin
  { 1. EIGENE ABFRAGEMASKE FÜR DIE KONFIGURATION ERSTELLEN }
  { Parameter: CreateInputQueryPage(NachWelcherSeite, Titel, Untertitel, Beschreibung) }
  ConfigPage := CreateInputQueryPage(wpSelectDir, 
    'Repository Konfiguration', 
    'Es werden einige Daten für das Repository benötigt.', '');
  
  ConfigPage.Add('Repository URL:', False);
  ConfigPage.Add('Benutzername:', False);
  ConfigPage.Add('Passwort/Token:', False);
  ConfigPage.Add('Lokaler Pfad:', False);
  
  ConfigPage.Values[0] := '';
  ConfigPage.Values[1] := '';
  ConfigPage.Values[2] := '';
  ConfigPage.Values[3] := 'Repository';
  
  { 2. DOWNLOAD-SEITE INITIALISIEREN }
  DownloadPage := CreateDownloadPage('Java herunterladen', 'Bitte warten Sie, während die Java ZIP geladen wird...', @OnDownloadProgress);
end;

function NextButtonClick(CurPageID: Integer): Boolean;
var
  ResultCode: Integer;
  JavaInstalled: Boolean;
begin
  Result := True;
  
  { 2. BEIM VERLASSEN DER WILLKOMMENS-SEITE: JAVA PRÜFEN }
  if CurPageID = wpWelcome then
  begin
    { Testet, ob 'java' in der Kommandozeile antwortet }
    JavaInstalled := ExecAsOriginalUser('cmd.exe', '/c java -version', '', SW_HIDE, ewWaitUntilTerminated, ResultCode) and (ResultCode = 0);
    
    if not JavaInstalled then
    begin
      JavaIsMissing := True;
      if MsgBox('Java wurde auf diesem System nicht gefunden. Java wird mit installieren?', mbInformation, MB_OKCANCEL) = idCancel then
      begin
	    WizardForm.Close; 
        Result := False;
        exit;
      end;
    end;
  end;
  

  { LIVE-DOWNLOAD STARTEN: Passiert kurz vor dem eigentlichen Installationsschritt }
  if (CurPageID = wpReady) and JavaIsMissing then
  begin
    DownloadPage.Clear;
    { Offizielle URL zum Windows x64 .msi Installer (hier als Beispiel Adoptium OpenJDK 21) }
    DownloadPage.Add('{#JAVA_URL}', 'java_runtime.zip', '');
    DownloadPage.Show;
    try
      DownloadPage.Download; { Startet den Download in den temporären Windows-Ordner }
      Result := True;
    except
        SuppressibleMsgBox(AddPeriod(GetExceptionMessage), mbCriticalError, MB_OK, IDOK);
        Result := False;
    finally
      DownloadPage.Hide;
    end;
  end;
end;

procedure CurStepChanged(CurStep: TSetupStep);
var
	ResultCode: Integer;
	PowerShellCmd: String;
	TomlPfad: string;
begin
	if CurStep = ssPostInstall then
	begin
		{ JAVA AUS DER GELADENEN ZIP ENTPACKEN }
		if JavaIsMissing and FileExists(ExpandConstant('{tmp}\java_runtime.zip')) then
		begin
			WizardForm.StatusLabel.Caption := 'Entpacke Java ZIP-Archiv...';
			
			{ Entpackt die 7za.exe vorab in den Temp-Ordner }
			ExtractTemporaryFile('7za.exe');
			
			{ 7-Zip-Befehl zusammenbauen (-y bestätigt alle Abfragen automatisch) }
			PowerShellCmd := FmtMessage('x "%1\java_runtime.zip" -o"%2" -y', [ExpandConstant('{tmp}'), ExpandConstant('{app}')]);
			
			{ Inno Setup blendet hier automatisch ein DOS-Fenster ein, wenn wir SW_SHOW wählen, }
			{ oder wir nutzen SW_HIDE. 7-Zip ist so schnell, dass es oft nur wenige Sekunden dauert! }
			Exec(ExpandConstant('{tmp}\7za.exe'), PowerShellCmd, '', SW_SHOW, ewWaitUntilTerminated, ResultCode);
		end;
		
		TomlPfad := ExpandConstant('{app}\vivien-server.toml');
    
		if FileExists(TomlPfad) then
		begin
			{ Aufruf der Replace-Funktion für jeden Parameter }
			ReplaceTextInFile(TomlPfad, '<GIT_URL>', ConfigPage.Values[0]);
			ReplaceTextInFile(TomlPfad, '<Git_USER>', ConfigPage.Values[1]);
			ReplaceTextInFile(TomlPfad, '<GIT_PASS>', ConfigPage.Values[2]);
			ReplaceTextInFile(TomlPfad, '<REPOSITORY>', ConfigPage.Values[3]);
		end;
	end;
end;

function ShouldAddPath: Boolean;
var
	OrigPath: string;
begin
	{ Wenn Java bereits auf dem System des Kunden installiert war, tun wir gar nichts }
	if not JavaIsMissing then
	begin
		Result := False;
		exit;
	end;

	{ Wenn Java fehlt, prüfen wir zur Sicherheit, ob genau DIESER Pfad schon existiert }
	if not RegQueryStringValue(HKEY_CURRENT_USER, 'Environment', 'Path', OrigPath) then
	begin
		Result := True;
		exit;
	end;
	Result := Pos(ExpandConstant('{app}\jdk-{#JAVA_VERSION}\bin'), OrigPath) = 0;
end;
