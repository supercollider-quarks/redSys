//redFrik

//related:
//RedEffectModule RedInstrumentModule RedAbstractMix

RedAbstractModule {	//abstract class
	classvar <all;
	var <group, addAction, <cvs, <specs;
	var condition, controllers;
	*initClass {
		all= List.new;
	}
	*new {|group, addAction|
		^super.new.initRedAbstractModule(group, addAction);
	}
	initRedAbstractModule {|argGroup, argAddAction|
		var server;
		condition= Condition();
		if(argGroup.isNil, {
			server= Server.default;
		}, {
			group= argGroup;
			server= group.server;
		});
		addAction= argAddAction;
		cvs= ();
		specs= ();
		controllers= List.new;
		this.def.metadata[\order].do{|assoc|
			var k= assoc.key;
			var v= assoc.value;
			var spec= this.def.metadata[\specs][k];
			if(k==\out, {v= \out});	//special case
			cvs.put(v, Ref(spec.default));
			specs.put(k, spec);
		};
		this.initMethods;
		forkIfNeeded{
			server.bootSync;
			this.def.add;
			server.sync;
			condition.test= true;
			condition.signal;
			all.add(this);
		};
	}
	initMethods {
		cvs.keysValuesDo{|k, v|
			this.addUniqueMethod((k++"_").asSymbol, {|obj, val|
				cvs[k].value_(specs[this.cvsToParam(k)].constrain(val)).changed(\value);
				this;
			});
			this.addUniqueMethod(k, {|obj|
				cvs[k].value;
			});
		};
	}
	cvsToParam {|name|
		if(name==\out, {
			^\out;
		}, {
			^this.def.metadata[\order].detect{|x| x.value==name}.key;
		});
	}
	free {
		controllers.do{|x| x.remove};
		all.remove(this);
	}
	def {^this.class.def}

	//--for subclasses
	*def {^this.subclassResponsibility(thisMethod)}
	gui {|parent, position| ^this.subclassResponsibility(thisMethod)}
}
