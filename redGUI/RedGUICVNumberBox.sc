//redFrik

RedGUICVNumberBox : RedGUICV {
	classvar <defaultWidth= 40, <defaultHeight= 14;

	//--private
	prMake {|parent, bounds|
		bounds= (bounds ?? {Rect(0, 0, defaultWidth, defaultHeight)}).asRect;
		^RedNumberBox(parent, bounds);
	}
}
