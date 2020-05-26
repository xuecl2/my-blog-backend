package xuecl.myblog.entity;

public class BlogEntity {
	private Long id;
	private String blogTitle;
	private String blogKeyWord;
	private String blogContent;
	private String createDate;
	private String createUser;
	private String modifyDate;
	private String modifyUser;
	private String isDel;
	private String blogDigest;
	
	public BlogEntity(Long id, String blogTitle, String blogKeyWord, String blogContent, String createDate,
			String createUser, String modifyDate, String modifyUser, String isDel, String blogDigest) {
		super();
		this.id = id;
		this.blogTitle = blogTitle;
		this.blogKeyWord = blogKeyWord;
		this.blogContent = blogContent;
		this.createDate = createDate;
		this.createUser = createUser;
		this.modifyDate = modifyDate;
		this.modifyUser = modifyUser;
		this.isDel = isDel;
		this.blogDigest = blogDigest;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getBlogTitle() {
		return blogTitle;
	}
	public void setBlogTitle(String blogTitle) {
		this.blogTitle = blogTitle;
	}
	public String getBlogKeyWord() {
		return blogKeyWord;
	}
	public void setBlogKeyWord(String blogKeyWord) {
		this.blogKeyWord = blogKeyWord;
	}
	public String getBlogContent() {
		return blogContent;
	}
	public void setBlogContent(String blogContent) {
		this.blogContent = blogContent;
	}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public String getCreateUser() {
		return createUser;
	}
	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}
	public String getModifyDate() {
		return modifyDate;
	}
	public void setModifyDate(String modifyDate) {
		this.modifyDate = modifyDate;
	}
	public String getModifyUser() {
		return modifyUser;
	}
	public void setModifyUser(String modifyUser) {
		this.modifyUser = modifyUser;
	}
	public String getIsDel() {
		return isDel;
	}
	public void setIsDel(String isDel) {
		this.isDel = isDel;
	}
	public String getBlogDigest() {
		return blogDigest;
	}
	public void setBlogDigest(String blogDigest) {
		this.blogDigest = blogDigest;
	}
	@Override
	public String toString() {
		return "BlogEntity [id=" + id + ", blogTitle=" + blogTitle + ", blogKeyWord=" + blogKeyWord + ", blogContent="
				+ blogContent + ", createDate=" + createDate + ", createUser=" + createUser + ", modifyDate="
				+ modifyDate + ", modifyUser=" + modifyUser + ", isDel=" + isDel + ", blogDigest=" + blogDigest + "]";
	}
	
}
