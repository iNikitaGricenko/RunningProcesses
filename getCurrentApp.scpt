global frontApp, frontAppName, windowTitle

tell application "System Events"
    set frontApp to first application process whose frontmost is true
    set frontAppName to name of frontApp
	    tell process frontAppName\n" +
		tell (1st window whose value of attribute "AXMain" is true)" +
		    set windowTitle to value of attribute "AXTitle"
		end tell
	end tell
end tell

return {frontAppName, windowTitle}
