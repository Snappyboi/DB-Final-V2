package backend;

// Simple Member model (ID + name)
public class Member {
    private int MemberID;
    private String memberName;

    public Member(int memberID, String memberName) {
        this.MemberID = memberID;
        this.memberName = memberName;
    }

    public int getMemberID() { return MemberID; }
    public String getMemberName() { return memberName; }
}
