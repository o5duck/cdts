package mvc.model.service.CRUD;

import java.sql.Connection;
import java.sql.SQLException;

import jdbc.JdbcUtil;
import jdbc.connection.ConnectionProvider;
import mvc.model.dao.ApprovalDesDao;
import mvc.model.dao.DocumentDao;
import mvc.model.dao.PersonalDao;
import mvc.model.exception.writeException.WriteException;
import mvc.model.vo.DocumentVo;
import mvc.model.vo.WriteResultVo;

public class WriteApprovalDocService implements WriteDocService {

	@Override
	public WriteResultVo service(DocumentVo docVo) throws SQLException {
		// TODO Auto-generated method stub
		Connection conn = ConnectionProvider.getConnection();
		try{
			String msg = validate(docVo);
			if(msg.equals("success")){
				conn.setAutoCommit(false);
				DocumentDao docDao = new DocumentDao();
				PersonalDao personalDao = new PersonalDao();
				ApprovalDesDao approvalDesDao = new ApprovalDesDao();
				
				int docId = docDao.insert(conn, docVo);
				if(docId == -1){
					throw new WriteException();
				}
		
				if(personalDao.insert(conn, docId, docVo.getGen_user(), "R_SEND", "결재대기")==-1)
					throw new WriteException();
				if(personalDao.insert(conn, docId, docVo.getDes1(), "R_NEW", "결재대기")==-1)
					throw new WriteException();
				if(approvalDesDao.insert(conn, docId, docVo.getDes1(), docVo.getDes2(), 1)==0)
					throw new WriteException();
				
				conn.commit();
				return new WriteResultVo(docId, msg);
			}else{
				return new WriteResultVo(0, msg);
			}
		}catch(SQLException e){
			JdbcUtil.rollback(conn);
			throw new WriteException();
		}catch(RuntimeException e){
			JdbcUtil.rollback(conn);
			throw new WriteException();
		}finally{
			JdbcUtil.close(conn);
		}
	}

	public String validate(DocumentVo doc){
		String msg = "success";
		if(doc.getTitle().equals("") || doc.getContent().equals("")){
			msg = "제목과 내용은 필수입력사항입니다.";
		}else if(doc.getDes1() == null){
			System.out.println("getdes1");
			msg = "첫번째 수신자는 필수입력사항입니다.";
		}else if(doc.getDes1()!=null&&doc.getDes2()==null&&doc.getDes3()!=null){
			System.out.println("getget");
			msg = "중간 수신자를 생략할 수 없습니다.";
		}
		return msg;
	}
}
