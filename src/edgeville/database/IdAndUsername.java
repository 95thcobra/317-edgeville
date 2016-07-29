package edgeville.database;

public class IdAndUsername {
	private int memberId;
	private String memberName;
	
	public IdAndUsername(int memberId, String memberName) {
		this.setMemberId(memberId);
		this.setMemberName(memberName);
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}
	
	
}
