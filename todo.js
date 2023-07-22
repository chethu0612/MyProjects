let taskInput = document.getElementById("taskInput");
let taskList = document.getElementById("taskList");

function addTask() {
  let taskText = taskInput.value.trim();
  if (taskText !== "") {
    let taskItem = document.createElement("li");
    taskItem.className = "list-group-item";
    taskItem.textContent = taskText;
    taskList.appendChild(taskItem);
    taskInput.value = "";
  }
}

taskInput.addEventListener("keyup", function(event) {
  if (event.keyCode === 13) {
    addTask();
  }
});
