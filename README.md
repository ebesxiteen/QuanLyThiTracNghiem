# Quizz System

Ứng dụng JavaFX client/server cho quản lý ngân hàng câu hỏi, tổ chức ca thi trắc nghiệm và ghi nhận bài nộp của sinh viên. Server quản lý dữ liệu, phát đề thi qua TCP socket; client nhận đề, xử lý lựa chọn đáp án và gửi kết quả về server để lưu vào MySQL.

## Tech Stack

| Layer | Technology |
| --- | --- |
| Language | Java 17 |
| UI | JavaFX 21, FXML |
| Build Tool | Maven |
| Database | MySQL 8.x |
| Data Access | JDBC, DAO Pattern |
| Serialization | Java Object Stream, Gson, Jackson |
| Office Import/Export | Apache POI |

## Database Architecture

Database chính: `QuizzDB`.

### Core Entities

| Table | Purpose | Main Relationships |
| --- | --- | --- |
| `Workspaces` | Không gian quản lý dữ liệu theo đơn vị/lớp/trường | 1-n với `SGroups`, `Subjects` |
| `SGroups` | Nhóm sinh viên trong một workspace | n-1 với `Workspaces`, 1-n với `Students`, `HostExams` |
| `Students` | Hồ sơ sinh viên | n-1 với `SGroups`, 1-n với `Submissions` |
| `Subjects` | Môn học/chủ đề câu hỏi | n-1 với `Workspaces`, 1-n với `Questions`, `Exams` |
| `Questions` | Câu hỏi trắc nghiệm | n-1 với `Subjects`; danh sách đáp án lưu dạng JSON trong `Answers` |
| `Exams` | Đề thi được tạo từ tập câu hỏi | n-1 với `Subjects`; danh sách câu hỏi lưu trong `QuestionIDs` |
| `HostExams` | Phiên thi được mở cho một nhóm | n-1 với `Exams`, `SGroups`; snapshot đề thi lưu JSON trong `ExamQuestions` |
| `Submissions` | Bài nộp của sinh viên | n-1 với `HostExams`, `Students`; đáp án đã chọn lưu JSON trong `AnswerSelecteds` |

### Data Storage Flows

1. **Workspace setup**
   - Tạo `Workspaces`.
   - Tạo `SGroups` và `Subjects` gắn với `WorkspaceID`.
   - Tạo `Students` gắn với `GroupID`.

2. **Question bank and exam creation**
   - Tạo `Questions` theo `SubjectID`.
   - Mỗi câu hỏi lưu nội dung, chương, độ khó và danh sách đáp án dạng JSON.
   - Tạo `Exams` bằng cách lưu danh sách `QuestionIDs` theo môn học.

3. **Exam hosting**
   - Server tạo `HostExams` từ `ExamID` và `GroupID`.
   - Snapshot câu hỏi của phiên thi được serialize vào `ExamQuestions` để cố định đề tại thời điểm mở thi.
   - Server mở TCP port và gửi đối tượng `HostExam` cho client sau khi xác thực mã sinh viên trong nhóm.

4. **Submission persistence**
   - Client tính thời gian làm bài và điểm dựa trên đáp án đã chọn.
   - Client gửi `Submission` về server qua `ObjectOutputStream`.
   - Server map `StudentID` sang `UID`, serialize đáp án vào `AnswerSelecteds`, sau đó ghi vào `Submissions`.

## Main Features

- Quản lý workspace, nhóm sinh viên, sinh viên, môn học, câu hỏi và đề thi.
- Tạo phiên thi theo đề thi, nhóm sinh viên, thời lượng, điểm tối đa và tùy chọn trộn câu hỏi.
- Client kết nối server qua IP/port để nhận đề và nộp bài.
- Tự động chấm điểm dựa trên đáp án đúng/sai của từng câu.
- Lưu bài nộp gồm thời gian làm bài, điểm số và chi tiết đáp án đã chọn.
- Hỗ trợ soft delete bằng cột `Archived` cho các bảng nghiệp vụ chính như `Workspaces`, `SGroups`, `Subjects`, `Questions`, `Exams`.

## Prerequisites

- Java JDK 17
- Maven 3.8+
- MySQL 8.0+
- JavaFX runtime tương thích với JavaFX Maven Plugin

## Installation & Setup

Clone repository:

```bash
git clone <repository-url>
cd Java-project-final
```

Build server:

```bash
cd QuizzServer
mvn clean package
```

Run server UI:

```bash
mvn javafx:run -Djavafx.mainClass=app.Launcher
```

Build client:

```bash
cd ../QuizzClient
mvn clean package
```

Run client UI:

```bash
mvn javafx:run -Djavafx.mainClass=application.Launcher
```

## Environment Configuration

Dự án đọc cấu hình từ Java system properties hoặc environment variables. Không commit password database thật vào repository.

File server:

```text
QuizzServer/src/main/java/utils/Constant.java
```

File client:

```text
QuizzClient/src/main/java/utils/Constant.java
```

Cấu hình runtime nên được truyền qua biến môi trường hoặc VM options:

```bash
set DB_URL=jdbc:mysql://localhost:3306/QuizzDB
set DB_USERNAME=root
set DB_PASSWORD=<your_database_password>
```

File `.env.example` là template cấu hình khi triển khai qua shell, IDE run configuration hoặc CI:

```bash
copy .env.example .env
```

Các key cấu hình:

| Key | Description | Example |
| --- | --- | --- |
| `DB_URL` | JDBC URL tới MySQL database | `jdbc:mysql://localhost:3306/QuizzDB` |
| `DB_USERNAME` | MySQL username | `root` |
| `DB_PASSWORD` | MySQL password | `<your_database_password>` |
| `SERVER_HOST` | IP server để client kết nối | `192.168.1.10` |
| `SERVER_PORT` | Port phiên thi do server mở | `5000` |

## Database Setup

Khởi tạo schema và seed data từ script SQL:

```bash
mysql -u root -p < QuizzServerInitTableMySQL.sql
```

Script sẽ:

- Drop và tạo lại database `QuizzDB`.
- Tạo các bảng: `Workspaces`, `SGroups`, `Students`, `Subjects`, `Questions`, `Exams`, `HostExams`, `Submissions`.
- Khai báo khóa ngoại với `ON DELETE CASCADE` và `ON UPDATE CASCADE`.
- Seed dữ liệu mẫu cho workspace, nhóm, sinh viên, môn học và câu hỏi.

## API Endpoints

Dự án không expose REST API hoặc Swagger. Luồng ghi dữ liệu được thực hiện qua JavaFX controller, service layer, DAO/JDBC và TCP socket trong phiên thi.

| Method | Endpoint / Entry Point | Persistence Target | Description |
| --- | --- | --- | --- |
| Internal DAO | `WorkspaceManager` / `WorkspaceDAO` | `Workspaces` | Tạo workspace và mã PIN quản lý. |
| Internal DAO | `QuestionManager` / `QuestionDAO` | `Questions` | Lưu câu hỏi, độ khó, chương và đáp án JSON. |
| Internal DAO | `HostExamManager` / `HostExamDAO` | `HostExams` | Tạo phiên thi từ đề thi và nhóm sinh viên. |
| TCP Socket | `StartClient` -> `StartServer` -> `SubmissionDAO` | `Submissions` | Gửi bài làm qua socket, tính điểm và lưu đáp án đã chọn. |

## Project Structure

```text
Java-project-final/
|-- QuizzServer/
|   |-- src/main/java/app/             # JavaFX server launcher
|   |-- src/main/java/controllers/     # UI controllers
|   |-- src/main/java/services/        # Business logic and socket server
|   |-- src/main/java/data/            # DAO layer
|   |-- src/main/java/model/           # Domain models
|   `-- src/main/resources/            # FXML, CSS, images
|-- QuizzClient/
|   |-- src/main/java/application/     # JavaFX client launcher
|   |-- src/main/java/controller/      # Client UI controllers
|   |-- src/main/java/services/        # Socket client and submission flow
|   |-- src/main/java/model/           # Shared client-side models
|   `-- src/main/resources/            # FXML, CSS, images
`-- QuizzServerInitTableMySQL.sql      # MySQL schema and seed data
```

## Runtime Flow

```text
Admin opens QuizzServer
  -> selects workspace
  -> manages groups, students, subjects, questions, exams
  -> creates HostExam
  -> server opens TCP port

Student opens QuizzClient
  -> enters server IP, port and student ID
  -> client sends student ID to server
  -> server validates student against HostExam group
  -> server sends HostExam payload
  -> client renders questions
  -> student submits answers
  -> client sends Submission payload
  -> server persists result into Submissions
```
