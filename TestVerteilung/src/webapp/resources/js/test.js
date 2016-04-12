function Script(){};
Script.prototype.test = function(str){
	function abcd(){
		return 1;
	}
	function cde(){
		return abcd();
	}
return 0+str;
};