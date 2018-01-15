//redFrik

RedGUICVKnob : RedGUICV {
	classvar <defaultWidth= 48, <defaultHeight= 48;

	//--private
	prMake {|parent, bounds|
		bounds= (bounds ?? {Rect(0, 0, defaultWidth, defaultHeight)}).asRect;
		^RedKnob(parent, bounds);
	}
}
