//redFrik

RedGUICVButton : RedGUICV {
	classvar <defaultWidth= 48, <defaultHeight= 14;

	//--private
	prMake {|parent, bounds|
		args= args ?? {(str: "")};
		^RedButton(parent, bounds, args[\str], args[\str]);
	}
}
