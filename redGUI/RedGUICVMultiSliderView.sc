//redFrik

RedGUICVMultiSliderView : RedGUICV {
	classvar <defaultWidth= 25,<defaultHeight= 25;

	//--private
	prMake {|parent, bounds|
		var m;
		args= args ?? {(num: 1)};
		bounds= (bounds ?? {Rect(0, 0, defaultWidth, defaultHeight*args[\num])}).asRect;
		m= RedMultiSliderView(parent, bounds);
		m.indexIsHorizontal= false;
		m.indexThumbSize= bounds.height/args[\num]-4;
		m.valueThumbSize= 0;
		m.gap= 4;
		m.isFilled= true;
		^m;
	}
}
