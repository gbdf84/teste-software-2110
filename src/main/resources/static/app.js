const apiUrl = '/api/usuarios';
const form = document.querySelector('#user-form');
const feedback = document.querySelector('#feedback');
const userList = document.querySelector('#user-list');
const userCount = document.querySelector('#user-count');
const submitButton = document.querySelector('#submit-button');
const submitLabel = document.querySelector('#submit-label');
const passwordInput = document.querySelector('#senha');
const cancelEditButton = document.querySelector('#cancel-edit');
const cpfInput = document.querySelector('#cpf');
const birthDateInput = document.querySelector('#dataNascimento');
const nameInput = document.querySelector('#nome');
let editingId = null;
let feedbackTimer;

function isValidCpf(cpf) {
  if (!/^\d{11}$/.test(cpf) || /^([0-9])\1{10}$/.test(cpf)) return false;
  const digit = (length) => {
    let sum = 0;
    for (let i = 0; i < length; i++) sum += Number(cpf[i]) * (length + 1 - i);
    const remainder = sum % 11;
    return remainder < 2 ? 0 : 11 - remainder;
  };
  return Number(cpf[9]) === digit(9) && Number(cpf[10]) === digit(10);
}

function isValidName(name) {
  return /^[\p{L}\p{M}]+(?: +[\p{L}\p{M}]+)*$/u.test(name.trim());
}

nameInput.addEventListener('input', () => {
  nameInput.setCustomValidity(nameInput.value && !isValidName(nameInput.value)
    ? 'Use somente letras e espaços no nome.' : '');
});

function localDateWithYearOffset(offset) {
  const now = new Date();
  const year = now.getFullYear() + offset;
  const month = now.getMonth();
  const day = Math.min(now.getDate(), new Date(year, month + 1, 0).getDate());
  return `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
}

function validateBirthDate() {
  const value = birthDateInput.value;
  if (!value) {
    birthDateInput.setCustomValidity('Informe a data de nascimento.');
  } else if (value > localDateWithYearOffset(-18)) {
    birthDateInput.setCustomValidity('O usuário deve ter pelo menos 18 anos.');
  } else if (value < localDateWithYearOffset(-120)) {
    birthDateInput.setCustomValidity('A data deve estar dentro dos últimos 120 anos.');
  } else {
    birthDateInput.setCustomValidity('');
  }
}

birthDateInput.min = localDateWithYearOffset(-120);
birthDateInput.max = localDateWithYearOffset(-18);

cpfInput.addEventListener('input', () => {
  cpfInput.value = cpfInput.value.replace(/\D/g, '').slice(0, 11);
  cpfInput.setCustomValidity(cpfInput.value && !isValidCpf(cpfInput.value)
    ? 'Informe um CPF válido, com os dígitos verificadores corretos.' : '');
});

birthDateInput.addEventListener('input', validateBirthDate);
birthDateInput.addEventListener('change', validateBirthDate);

function showFeedback(message, isError = false) {
  feedback.textContent = message;
  feedback.classList.toggle('error', isError);
  feedback.hidden = false;
  clearTimeout(feedbackTimer);
  feedbackTimer = setTimeout(() => { feedback.hidden = true; }, 5000);
}

async function request(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: { ...(options.body ? { 'Content-Type': 'application/json' } : {}), ...options.headers }
  });
  if (!response.ok) {
    let message = 'Não foi possível concluir a operação.';
    try {
      const body = await response.json();
      message = body.detail || body.message || body.error || message;
    } catch { /* A API pode responder sem corpo JSON. */ }
    throw new Error(message);
  }
  return response.status === 204 ? null : response.json();
}

function formatDate(date) {
  if (!date) return '—';
  const [year, month, day] = date.split('-');
  return `${day}/${month}/${year}`;
}

function makeCell(value) {
  const cell = document.createElement('td');
  cell.textContent = value;
  return cell;
}

function renderUsers(users) {
  userCount.textContent = users.length;
  userList.replaceChildren();
  if (!users.length) {
    const row = document.createElement('tr');
    const cell = document.createElement('td');
    cell.colSpan = 4;
    cell.className = 'empty-cell';
    cell.textContent = 'Nenhum usuário cadastrado ainda.';
    row.append(cell);
    userList.append(row);
    return;
  }
  for (const user of users) {
    const row = document.createElement('tr');
    row.append(makeCell(user.nome), makeCell(user.cpf), makeCell(formatDate(user.dataNascimento)));
    const actions = document.createElement('td');
    const group = document.createElement('div');
    group.className = 'row-actions';
    const edit = document.createElement('button');
    edit.type = 'button';
    edit.className = 'icon-button';
    edit.title = `Editar ${user.nome}`;
    edit.setAttribute('aria-label', `Editar ${user.nome}`);
    edit.textContent = '✎';
    edit.addEventListener('click', () => startEdit(user));
    const remove = document.createElement('button');
    remove.type = 'button';
    remove.className = 'icon-button delete';
    remove.title = `Excluir ${user.nome}`;
    remove.setAttribute('aria-label', `Excluir ${user.nome}`);
    remove.textContent = '×';
    remove.addEventListener('click', () => deleteUser(user));
    group.append(edit, remove);
    actions.append(group);
    row.append(actions);
    userList.append(row);
  }
}

async function loadUsers() {
  userList.innerHTML = '<tr><td class="empty-cell" colspan="4">Carregando usuários…</td></tr>';
  try {
    renderUsers(await request(apiUrl));
  } catch (error) {
    userCount.textContent = '—';
    const row = document.createElement('tr');
    const cell = document.createElement('td');
    cell.colSpan = 4;
    cell.className = 'empty-cell';
    cell.textContent = 'Não foi possível carregar os usuários. Verifique se o servidor e o banco de dados estão disponíveis e tente novamente.';
    row.append(cell);
    userList.replaceChildren(row);
    showFeedback(`Erro ao carregar usuários: ${error.message}`, true);
  }
}

function startEdit(user) {
  editingId = user.id;
  form.elements.nome.value = user.nome;
  form.elements.cpf.value = user.cpf;
  cpfInput.readOnly = true;
  document.querySelector('#cpf-hint').hidden = false;
  form.elements.dataNascimento.value = user.dataNascimento;
  passwordInput.value = '';
  passwordInput.required = true;
  document.querySelector('#password-hint').textContent = '· digite novamente para atualizar';
  passwordInput.placeholder = 'Nova senha (mínimo de 8 caracteres)';
  submitLabel.textContent = 'Salvar alterações';
  document.querySelector('#form-title').textContent = 'Editar usuário';
  cancelEditButton.hidden = false;
  form.scrollIntoView({ behavior: 'smooth', block: 'center' });
  form.elements.nome.focus({ preventScroll: true });
}

function resetForm() {
  form.reset();
  nameInput.setCustomValidity('');
  cpfInput.setCustomValidity('');
  cpfInput.readOnly = false;
  document.querySelector('#cpf-hint').hidden = true;
  birthDateInput.setCustomValidity('');
  editingId = null;
  passwordInput.required = true;
  passwordInput.placeholder = 'Mínimo de 8 caracteres';
  document.querySelector('#password-hint').textContent = '';
  submitLabel.textContent = 'Salvar usuário';
  document.querySelector('#form-title').textContent = 'Adicionar usuário';
  cancelEditButton.hidden = true;
}

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  nameInput.setCustomValidity(isValidName(nameInput.value) ? '' : 'Use somente letras e espaços no nome.');
  cpfInput.setCustomValidity(isValidCpf(cpfInput.value) ? '' : 'Informe um CPF válido, com os dígitos verificadores corretos.');
  validateBirthDate();
  if (!form.reportValidity()) return;
  const data = new FormData(form);
  const payload = {
    nome: data.get('nome').trim(),
    cpf: data.get('cpf').trim(),
    dataNascimento: data.get('dataNascimento'),
    senha: data.get('senha')
  };
  submitButton.disabled = true;
  try {
    await request(editingId ? `${apiUrl}/${editingId}` : apiUrl, {
      method: editingId ? 'PUT' : 'POST',
      body: JSON.stringify(payload)
    });
    showFeedback(editingId ? 'Cadastro atualizado com sucesso.' : 'Usuário cadastrado com sucesso.');
    resetForm();
    await loadUsers();
  } catch (error) {
    showFeedback(error.message, true);
  } finally {
    submitButton.disabled = false;
  }
});

cancelEditButton.addEventListener('click', resetForm);
document.querySelector('#refresh-button').addEventListener('click', loadUsers);

async function deleteUser(user) {
  if (!window.confirm(`Excluir o cadastro de ${user.nome}? Esta ação não pode ser desfeita.`)) return;
  try {
    await request(`${apiUrl}/${user.id}`, { method: 'DELETE' });
    if (editingId === user.id) resetForm();
    showFeedback('Usuário excluído com sucesso.');
    await loadUsers();
  } catch (error) {
    showFeedback(error.message, true);
  }
}

loadUsers();
