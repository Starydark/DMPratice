package JSONFile;

public class Data {
	public int build_id;
	public int sha;
	double success;
	int index;
	String name;
	public int comment_date;
	public int comment_count;
	public int message_len;
	public int author_id;
	public int stats_add;
	public int stats_del;
	public int files_add;
	public int files_del;
	public int files_staus;
	public double filetype;
	public String build_result;
	
	public Data(){
		this.success = 0;
		this.message_len = 0;
		this.build_id = 0;
		this.sha = 0;
		this.index = 0;
		this.name = "";
		this.comment_date = 0;
		this.comment_count = 0;
		this.author_id = 0;
		this.stats_add = 0;
		this.stats_del = 0;
		this.files_add = 0;
		this.files_del = 0;
		this.files_staus = 0;		
		this.filetype = 0.0;		
		this.build_result = "";
	}
}
