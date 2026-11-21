package gui;

import dao.AnimalDAO;
import dao.ClienteDAO;
import dao.DAOException;
import dao.RacaDAO;
import modelo.Animal;
import modelo.Cliente;
import modelo.Raca;
import util.CalculadoraIdade;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.time.LocalDate;
import java.util.List;

/**
 * Tela de animais, com duas abas:
 *  - Cadastro: listar/pesquisar, cadastrar, alterar, desativar e reativar animais;
 *  - Consultar por cliente: busca por nome ou CPF do cliente, exibindo raça, idade, sexo e status.
 */
public class PainelAnimais extends JPanel implements Atualizavel {

    private static final long serialVersionUID = 1L;

    private static final String[] OPCOES_SEXO = {"Não informado", "Macho", "Fêmea"};

    private final AnimalDAO animalDAO = new AnimalDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final RacaDAO racaDAO = new RacaDAO();

    // ---- aba de cadastro
    private final ModeloTabela<Animal> modelo = new ModeloTabela<>(
            new String[]{"ID", "Animal", "Raça", "Cliente", "Nascimento", "Idade", "Sexo", "Cor", "Status"},
            a -> new Object[]{a.getId(), a.getNome(), a.getNomeRaca() + " (" + a.getTipoAnimal() + ")", a.getNomeCliente(),
                    Formatos.data(a.getDataNascimento()), CalculadoraIdade.formatar(a.getDataNascimento()),
                    Formatos.sexo(a.getSexo()), Formatos.vazioSeNulo(a.getCor()), Formatos.status(a.isStatus())});
    private final JTable tabela = Tema.tabela(modelo);
    private final BarraPesquisa barra = new BarraPesquisa("Pesquisar por nome do animal, raça ou cliente", this::pesquisar);
    private final JLabel lblContagem = Tema.subtitulo(" ");

    private final JLabel lblFormulario = Tema.secao("Novo animal");
    private final JTextField txtId = Tema.campo();
    private final JTextField txtNome = Tema.campo();
    private final JTextField txtNascimento = Tema.campo();
    private final JComboBox<String> cmbSexo = new JComboBox<>(OPCOES_SEXO);
    private final JComboBox<Cliente> cmbCliente = new JComboBox<>();
    private final JComboBox<Raca> cmbRaca = new JComboBox<>();
    private final JTextField txtCor = Tema.campo();
    private final JTextArea txtObservacoes = new JTextArea(3, 20);
    private final BotoesCrud botoes;

    private Animal selecionado;
    private boolean carregando;

    // ---- aba de consulta por cliente
    private final ModeloTabela<Animal> modeloConsulta = new ModeloTabela<>(
            new String[]{"Cliente", "CPF", "Animal", "Raça", "Idade", "Sexo", "Status"},
            a -> new Object[]{a.getNomeCliente(), a.getCpfCliente(), a.getNome(),
                    a.getNomeRaca() + " (" + a.getTipoAnimal() + ")",
                    CalculadoraIdade.formatar(a.getDataNascimento()),
                    Formatos.sexo(a.getSexo()), Formatos.status(a.isStatus())});
    private final JTable tabelaConsulta = Tema.tabela(modeloConsulta);
    private final BarraPesquisa barraConsulta = new BarraPesquisa("Pesquisar por nome ou CPF do cliente", this::pesquisarPorCliente);
    private final JLabel lblContagemConsulta = Tema.subtitulo(" ");

    public PainelAnimais() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Tema.FUNDO);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        botoes = new BotoesCrud(false, this::novo, this::salvar, this::desativar, this::reativar);

        JPanel cabecalho = new JPanel();
        cabecalho.setLayout(new BoxLayout(cabecalho, BoxLayout.Y_AXIS));
        cabecalho.setOpaque(false);
        JLabel titulo = Tema.titulo("Animais");
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub = Tema.subtitulo("Gerencie o cadastro de animais e consulte os pets vinculados a cada cliente.");
        sub.setAlignmentX(LEFT_ALIGNMENT);
        cabecalho.add(titulo);
        cabecalho.add(Box.createVerticalStrut(4));
        cabecalho.add(sub);

        JTabbedPane abas = new JTabbedPane();
        abas.setFont(Tema.FONTE_NEGRITO);
        abas.addTab("Cadastro", criarAbaCadastro());
        abas.addTab("Consultar por cliente", criarAbaConsulta());

        add(cabecalho, BorderLayout.NORTH);
        add(abas, BorderLayout.CENTER);

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
        Tema.estilizarCampo(cmbSexo);
        Tema.estilizarCampo(cmbCliente);
        Tema.estilizarCampo(cmbRaca);

        txtObservacoes.setFont(Tema.FONTE);
        txtObservacoes.setForeground(Tema.TEXTO);
        txtObservacoes.setLineWrap(true);
        txtObservacoes.setWrapStyleWord(true);
        txtObservacoes.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        novo();
    }

    @Override
    public void atualizar() {
        carregarCombos();
        pesquisar();
        pesquisarPorCliente();
    }

    /** Usado pelo atalho do painel inicial. */
    public void novoCadastro() {
        carregarCombos();
        novo();
        txtNome.requestFocusInWindow();
    }

    private JPanel criarAbaCadastro() {
        JPanel aba = new JPanel(new BorderLayout(0, 12));
        aba.setBackground(Tema.FUNDO);
        aba.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        JPanel topo = new JPanel(new BorderLayout(0, 6));
        topo.setOpaque(false);
        topo.add(barra, BorderLayout.NORTH);
        topo.add(lblContagem, BorderLayout.SOUTH);

        aba.add(topo, BorderLayout.NORTH);
        aba.add(Tema.rolagem(tabela), BorderLayout.CENTER);
        aba.add(criarFormulario(), BorderLayout.SOUTH);
        return aba;
    }

    private JPanel criarAbaConsulta() {
        JPanel aba = new JPanel(new BorderLayout(0, 12));
        aba.setBackground(Tema.FUNDO);
        aba.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        JPanel topo = new JPanel(new BorderLayout(0, 6));
        topo.setOpaque(false);
        topo.add(barraConsulta, BorderLayout.NORTH);
        topo.add(lblContagemConsulta, BorderLayout.SOUTH);

        aba.add(topo, BorderLayout.NORTH);
        aba.add(Tema.rolagem(tabelaConsulta), BorderLayout.CENTER);
        return aba;
    }

    private JPanel criarFormulario() {
        JPanel cartao = Tema.cartao();
        cartao.setLayout(new BorderLayout(0, 12));
        cartao.add(lblFormulario, BorderLayout.NORTH);

        JPanel grade = new JPanel(new GridBagLayout());
        grade.setOpaque(false);

        adicionarCampo(grade, 0, 0, 0.2, "ID", txtId);
        adicionarCampo(grade, 1, 0, 1.4, "Nome do animal *", txtNome);
        adicionarCampo(grade, 2, 0, 1.4, "Cliente (tutor) *", cmbCliente);
        adicionarCampo(grade, 3, 0, 1.2, "Raça *", cmbRaca);

        adicionarCampo(grade, 0, 1, 0.7, "Nascimento (dd/mm/aaaa)", txtNascimento);
        adicionarCampo(grade, 1, 1, 0.7, "Sexo", cmbSexo);
        adicionarCampo(grade, 2, 1, 0.8, "Cor", txtCor);

        JScrollPane scrollObs = new JScrollPane(txtObservacoes);
        scrollObs.setBorder(BorderFactory.createLineBorder(Tema.BORDA, 1));
        scrollObs.setPreferredSize(new Dimension(200, 34));
        adicionarCampo(grade, 3, 1, 1.6, "Observações", scrollObs);

        cartao.add(grade, BorderLayout.CENTER);
        cartao.add(botoes, BorderLayout.SOUTH);
        return cartao;
    }

    private void adicionarCampo(JPanel grade, int x, int y, double peso, String rotulo, JComponent comp) {
        JPanel box = new JPanel(new BorderLayout(0, 4));
        box.setOpaque(false);
        box.add(Tema.rotulo(rotulo), BorderLayout.NORTH);
        box.add(comp, BorderLayout.CENTER);

        GridBagConstraints c = Tema.gbc(x, y);
        c.weightx = peso;
        grade.add(box, c);
    }

    private void carregarCombos() {
        try {
            Cliente clienteAtual = (Cliente) cmbCliente.getSelectedItem();
            cmbCliente.removeAllItems();
            for (Cliente c : clienteDAO.pesquisar("", false)) {
                cmbCliente.addItem(c);
            }
            if (clienteAtual != null) {
                selecionarClientePorId(clienteAtual.getId());
            }

            Raca racaAtual = (Raca) cmbRaca.getSelectedItem();
            cmbRaca.removeAllItems();
            for (Raca r : racaDAO.pesquisar("", false)) {
                cmbRaca.addItem(r);
            }
            if (racaAtual != null) {
                selecionarRacaPorId(racaAtual.getId());
            }
        } catch (DAOException e) {
            Mensagens.erro(this, "Erro ao carregar clientes e raças: " + e.getMessage());
        }
    }

    private void selecionarClientePorId(int id) {
        for (int i = 0; i < cmbCliente.getItemCount(); i++) {
            if (cmbCliente.getItemAt(i).getId() == id) {
                cmbCliente.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selecionarRacaPorId(int id) {
        for (int i = 0; i < cmbRaca.getItemCount(); i++) {
            if (cmbRaca.getItemAt(i).getId() == id) {
                cmbRaca.setSelectedIndex(i);
                return;
            }
