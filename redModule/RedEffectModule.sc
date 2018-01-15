//redFrik

//--related:
//RedEffectModuleGUI RedEffectsRack RedAbstractMix RedEfxRing
//RedEffectModule.subclasses

RedEffectModule : RedAbstractModule {	//abstract class
	var <synth;
	*new {|out= 0, group, addAction= \addToTail|
		^super.new(group, addAction).initRedEffectModule(out);
	}
	initModule {|group|
		this.def.add;
		group.server.sync;
		all.add(this);
		super.initMethods;
		this.initRedEffectModule(cvs.out.value);
	}
	initRedEffectModule {|out|
		var args= List.new;
		forkIfNeeded{
			condition.wait;
			cvs.out.value= out;	//override spec default with bus argument
			cvs.keysValuesDo{|k, v|
				args.add(this.cvsToParam(k));
				args.add(v.value);
			};
			synth= Synth(this.def.name, args, group, addAction);
			this.prAddControllers;
		};
	}
	free {
		synth.free;
		super.free;
	}
	gui {|parent, position|
		^RedEffectModuleGUI(this, parent, position);
	}

	//--private
	prAddControllers {
		cvs.keysValuesDo{|k, v|
			var param= this.cvsToParam(k);
			controllers.add(
				SimpleController(v).put(\value, {|ref|
					synth.set(param, ref.value);
				})
			);
		};
	}

	//--for subclasses
	*def {^this.subclassResponsibility(thisMethod)}
}
