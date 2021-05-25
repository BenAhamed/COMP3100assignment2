public class Server {
    public String id;
	public String type;
	public String cores;
    public String status;

	Server(String id, String t, String c, String s) {
		this.id = id;
		this.type = t;
		this.cores = c;
        this.status = s;
	}
}
