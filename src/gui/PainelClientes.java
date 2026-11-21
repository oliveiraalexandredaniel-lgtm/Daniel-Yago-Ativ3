package gui;

import dao.ClienteDAO;
import dao.DAOException;
import modelo.Cliente;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.time.LocalDate;
import java.util.List;

/** Tela de clientes: listar/pesquisar por nome ou CPF, cadastrar, alterar, desativar e reativar. */
public class PainelClientes extends JPanel implements Atualizavel {

    private static final long serialVersionUID = 1L;

    private static final String[] UFS = {"", "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS",
            "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"};

    private final ClienteDAO dao = new ClienteDAO();

    private final ModeloTabela<Cliente> modelo = new ModeloTabela<>(
            new String[]{"ID", "Nome", "CPF", "Nascimento", "Telefone", "Cidade/UF", "Última alteração"},
            c -> new Object[]{c.getId(), c.getNome(), c.getCpf(), Formatos.data(c.getDataNascimento()),
                    Formatos.vazioSeNulo(c.getTelefone()), cidadeUf(c), Formatos.dataHora(c.getDataUltimaAlteracao())});
    private final JTable tabela = Tema.tabela(modelo);
    private final BarraPesquisa barra = new BarraPesquisa("Pesquisar por nome ou CPF", this::pesquisar);
    private final JLabel lblContagem = Tema.subtitulo(" ");

    private final JLabel lblFormulario = Tema.secao("Novo cliente");
    private final JTextField txtId = Tema.campo();
    private final JTextField txtNome = Tema.campo();
    private final JTextField txtCpf = Tema.campo();
    private final JTextField txtNascimento = Tema.campo();
    private final JTextField txtTelefone = Tema.campo();
    private final JTextField txtEndereco = Tema.campo();
    private final JTextField txtBairro = Tema.campo();
    private final JTextField txtCidade = Tema.campo();
    private final JComboBox<String> cmbEstado = new JComboBox<>(UFS);
    private final JTextField txtCep = Tema.campo();
    private final BotoesCrud botoes;

    private Cliente selecionado;
    private boolean carregando;

    public PainelClientes() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Tema.FUNDO);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        botoes = new BotoesCrud(false, this::novo, this::salvar, this::desativar, this::reativar);
        add(criarTopo(), BorderLayout.NORTH);
        add(Tema.rolagem(tabela), BorderLayout.CENTER);
        add(criarFormulario(), BorderLayout.SOUTH);

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !carregando) {
                int linha = tabela.getSelectedRow();
                if (linha >= 0) {
                    carregarNoFormulario(modelo.getItem(tabela.convertRowIndexToModel(linha)));
                }
            }
        });
        txtId.setEditable(false);
        txtId.setBackground(Tema.CAMPO_BLOQUEADO);
        txtId.setToolTipText("O ID é gerado pelo sistema e não pode ser alterado.");
        Tema.estilizarCampo(cmbEstado);

        novo();
    }

    private JPanel criarTopo() {
        JPanel topo = new JPanel(new BorderLayout(0, 10));
        topo.setOpaque(false);

        JPanel cabecalho = new JPanel();
        cabecalho.setLayout(new BoxLayout(cabecalho, BoxLayout.Y_AXIS));
        cabecalho.setOpaque(false);
        cabecalho.add(Tema.titulo("Clientes"));
        cabecalho.add(lblContagem);

        topo.add(cabecalho, BorderLayout.NORTH);
        topo.add(barra, BorderLayout.SOUTH);
        return topo;
    }

    private JPanel criarFormulario() {
        JPanel cartao = Tema.cartao();
        cartao.setLayout(new BorderLayout(0, 12));
        cartao.add(lblFormulario, BorderLayout.NORTH);

        JPanel grade = new JPanel(new GridBagLayout());
        grade.setOpaque(false);

        adicionarCampo(grade, 0, 0, 0.2, "ID", txtId);
        adicionarCampo(grade, 1, 0, 1.5, "Nome *", txtNome);
        adicionarCampo(grade, 2, 0, 0.8, "CPF *", txtCpf);
        adicionarCampo(grade, 3, 0, 0.7, "Nascimento (dd/mm/aaaa)", txtNascimento);
        adicionarCampo(grade, 4, 0, 0.8, "Telefone", txtTelefone);

        adicionarCampo(grade, 0, 1, 1.5, "Endereço", txtEndereco);
        adicionarCampo(grade, 1, 1, 0.9, "Bairro", txtBairro);
        adicionarCampo(grade, 2, 1, 0.9, "Cidade", txtCidade);
        adicionarCampo(grade, 3, 1, 0.4, "UF", cmbEstado);
        adicionarCampo(grade, 4, 1, 0.6, "CEP", txtCep);

        cartao.add(grade, BorderLayout.CENTER);
        cartao.add(botoes, BorderLayout.SOUTH);
        return cartao;
    }

    private void adicionarCampo(JPanel grade, int x, int y, double peso, String rotulo, java.awt.Component comp) {
        JPanel box = new JPanel(new BorderLayout(0, 4));
        box.setOpaque(false);
        box.add(Tema.rotulo(rotulo), BorderLayout.NORTH);
        box.add(comp, BorderLayout.CENTER);

        GridBagConstraints c = Tema.gbc(x, y);
        c.weightx = peso;
        grade.add(box, c);
    }

    @Override
    public void atualizar() {
        pesquisar();
    }

    private void pesquisar() {
        try {
            carregando = true;
            List<Cliente> lista = dao.pesquisar(barra.getTexto(), barra.isMostrarInativos());
            modelo.setItens(lista);
            lblContagem.setText(lista.size() + (lista.size() == 1 ? " cliente encontrado" : " clientes encontrados"));
            botoes.atualizar(selecionado != null, barra.isMostrarInativos());
        } catch (DAOException e) {
            Mensagens.erro(this, "Erro ao consultar clientes: " + e.getMessage());
        } finally {
            carregando = false;
        }
    }

    private void novo() {
        selecionado = null;
        tabela.clearSelection();
        lblFormulario.setText("Novo cliente");
        txtId.setText("");
        txtNome.setText("");
        txtCpf.setText("");
        txtNascimento.setText("");
        txtTelefone.setText("");
        txtEndereco.setText("");
        txtBairro.setText("");
        txtCidade.setText("");
        cmbEstado.setSelectedIndex(0);
        txtCep.setText("");
        botoes.atualizar(false, barra.isMostrarInativos());
        txtNome.requestFocus();
    }

    private void carregarNoFormulario(Cliente c) {
        selecionado = c;
        lblFormulario.setText("Editando cliente #" + c.getId());
        txtId.setText(String.valueOf(c.getId()));
        txtNome.setText(Formatos.vazioSeNulo(c.getNome()));
        txtCpf.setText(Formatos.vazioSeNulo(c.getCpf()));
        txtNascimento.setText(Formatos.data(c.getDataNascimento()));
        txtTelefone.setText(Formatos.vazioSeNulo(c.getTelefone()));
        txtEndereco.setText(Formatos.vazioSeNulo(c.getEndereco()));
        txtBairro.setText(Formatos.vazioSeNulo(c.getBairro()));
        txtCidade.setText(Formatos.vazioSeNulo(c.getCidade()));
        cmbEstado.setSelectedItem(Formatos.vazioSeNulo(c.getEstado()));
        txtCep.setText(Formatos.vazioSeNulo(c.getCep()));
        botoes.atualizar(true, barra.isMostrarInativos());
    }

    private void salvar() {
        try {
            String nome = txtNome.getText().trim();
            String cpf = txtCpf.getText().trim();
            if (nome.isEmpty() || cpf.isEmpty()) {
                Mensagens.aviso(this, "Preencha os campos obrigatórios: Nome e CPF.");
                return;
            }

            LocalDate nascimento = Formatos.lerData(txtNascimento.getText());

            Cliente c = (selecionado != null) ? selecionado : new Cliente();
            c.setNome(nome);
            c.setCpf(cpf);
            c.setDataNascimento(nascimento);
            c.setTelefone(txtTelefone.getText().trim());
            c.setEndereco(txtEndereco.getText().trim());
            c.setBairro(txtBairro.getText().trim());
            c.setCidade(txtCidade.getText().trim());
            c.setEstado((String) cmbEstado.getSelectedItem());
            c.setCep(txtCep.getText().trim());

            if (selecionado == null) {
                dao.inserir(c);
                Mensagens.info(this, "Cliente cadastrado com sucesso!");
            } else {
                dao.atualizar(c);
                Mensagens.info(this, "Cliente atualizado com sucesso!");
            }
            novo();
            pesquisar();
        } catch (IllegalArgumentException e) {
            Mensagens.aviso(this, e.getMessage());
        } catch (DAOException e) {
            Mensagens.erro(this, "Erro ao salvar cliente: " + e.getMessage());
        }
    }

    private void desativar() {
        if (selecionado == null) {
            return;
        }
        if (Mensagens.confirmar(this, "Deseja desativar o cliente \"" + selecionado.getNome() + "\"?")) {
            try {
                dao.desativar(selecionado.getId());
                novo();
                pesquisar();
            } catch (DAOException e) {
                Mensagens.erro(this, "Erro ao desativar cliente: " + e.getMessage());
            }
        }
    }

    private void reativar() {
        if (selecionado == null) {
            return;
        }
        if (Mensagens.confirmar(this, "Deseja reativar o cliente \"" + selecionado.getNome() + "\"?")) {
            try {
                dao.reativar(selecionado.getId());
                novo();
                pesquisar();
            } catch (DAOException e) {
                Mensagens.erro(this, "Erro ao reativar cliente: " + e.getMessage());
            }
        }
    }

    private static String cidadeUf(Cliente c) {
        String cidade = Formatos.vazioSeNulo(c.getCidade());
        String uf = Formatos.vazioSeNulo(c.getEstado());
        if (cidade.isEmpty()) {
            return uf;
        }
