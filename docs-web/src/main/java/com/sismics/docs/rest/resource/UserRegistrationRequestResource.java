// package com.sismics.docs.rest.resource;

// import com.sismics.docs.core.dao.UserRegistrationRequestDao;
// // import com.sismics.docs.core.dao.*;
// import com.sismics.docs.rest.constant.BaseFunction;
// import com.sismics.docs.core.dao.UserDao;
// import com.sismics.docs.core.model.jpa.UserRegistrationRequest;
// import com.sismics.docs.core.model.jpa.User;
// import com.sismics.rest.exception.ClientException;
// import com.sismics.rest.exception.ForbiddenClientException;

// import jakarta.json.Json;
// import jakarta.json.JsonObjectBuilder;
// import jakarta.ws.rs.*;
// import jakarta.ws.rs.core.MediaType;
// import jakarta.ws.rs.core.Response;
// import java.util.List;

// @Path("/user/register_request")
// public class UserRegistrationRequestResource extends BaseResource {
//     /**
//      * Guest submits a registration request.
//      */
//     @POST
//     @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
//     public Response registerRequest(
//         @FormParam("username") String username,
//         @FormParam("password") String password,
//         @FormParam("email") String email
//     ) {
//         // 1. 基本校验
//         if (username == null || password == null || email == null) {
//             throw new ClientException("ValidationError", "Username, password, and email are required");
//         }

//         // 2. 检查是否有同名pending请求
//         UserRegistrationRequestDao dao = new UserRegistrationRequestDao();
//         if (dao.findByUsername(username) != null) {
//             throw new ClientException("AlreadyRequested", "A pending request for this username already exists");
//         }

//         // 3. 检查是否有同名已存在用户
//         UserDao userDao = new UserDao();
//         if (userDao.getActiveByUsername(username) != null) {
//             throw new ClientException("AlreadyExistingUsername", "This username is already taken");
//         }

//         // 4. 构造注册请求对象
//         UserRegistrationRequest req = new UserRegistrationRequest();
//         req.setUsername(username);
//         req.setPassword(password); // 可选：此处可加密
//         req.setEmail(email);
//         req.setStatus("pending");

//         // 5. 保存请求
//         String id = dao.create(req);

//         // 6. 返回响应
//         JsonObjectBuilder resp = Json.createObjectBuilder()
//             .add("id", id)
//             .add("status", "pending");
//         return Response.ok(resp.build()).build();
//     }

//     /**
//      * Admin lists all pending registration requests.
//      */
//     @GET
//     @Path("/list")
//     public Response listPending() {
//         if (!authenticate() || !hasBaseFunction(BaseFunction.ADMIN)) {
//             throw new ForbiddenClientException();
//         }
//         UserRegistrationRequestDao dao = new UserRegistrationRequestDao();
//         List<UserRegistrationRequest> list = dao.listPending();
//         return Response.ok(list).build();
//     }

//     /**
//      * Admin accepts a registration request.
//      */
//     @POST
//     @Path("/{id}/accept")
//     public Response accept(@PathParam("id") String id) throws Exception {
//         if (!authenticate() || !hasBaseFunction(BaseFunction.ADMIN)) {
//             throw new ForbiddenClientException();
//         }
//         UserRegistrationRequestDao dao = new UserRegistrationRequestDao();
//         UserRegistrationRequest req = dao.getById(id);
//         if (req == null || !"pending".equals(req.getStatus())) {
//             throw new ClientException("NotFoundOrNotPending", "Request not found or not pending");
//         }
//         // 创建正式用户
//         UserDao userDao = new UserDao();
//         User user = new User();
//         user.setUsername(req.getUsername());
//         user.setPassword(req.getPassword()); // 密码加密如有需要可在此处理
//         user.setEmail(req.getEmail());
//         user.setRoleId("user");
//         userDao.create(user, principal.getId());
//         // 更新请求状态
//         req.setStatus("accepted");
//         dao.update(req);
//         return Response.ok(Json.createObjectBuilder().add("status", "accepted").build()).build();
//     }

//     /**
//      * Admin rejects a registration request.
//      */
//     @POST
//     @Path("/{id}/reject")
//     public Response reject(@PathParam("id") String id, @FormParam("comment") String comment) {
//         if (!authenticate() || !hasBaseFunction(BaseFunction.ADMIN)) {
//             throw new ForbiddenClientException();
//         }
//         UserRegistrationRequestDao dao = new UserRegistrationRequestDao();
//         UserRegistrationRequest req = dao.getById(id);
//         if (req == null || !"pending".equals(req.getStatus())) {
//             throw new ClientException("NotFoundOrNotPending", "Request not found or not pending");
//         }
//         req.setStatus("rejected");
//         req.setAdminComment(comment);
//         dao.update(req);
//         return Response.ok(Json.createObjectBuilder().add("status", "rejected").build()).build();
//     }

//     /**
//      * Guest/admin queries the status of a registration request.
//      */
//     @GET
//     @Path("/{id}/status")
//     public Response getStatus(@PathParam("id") String id) {
//         UserRegistrationRequestDao dao = new UserRegistrationRequestDao();
//         UserRegistrationRequest req = dao.getById(id);
//         if (req == null) {
//             throw new ClientException("NotFound", "Request not found");
//         }
//         JsonObjectBuilder resp = Json.createObjectBuilder().add("status", req.getStatus());
//         if (req.getAdminComment() != null) {
//             resp.add("adminComment", req.getAdminComment());
//         }
//         return Response.ok(resp.build()).build();
//     }
// } 