package gui;

import dao.DAOException;
import dao.RacaDAO;
import modelo.Raca;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

/** Tela de raças: listar/pesquisar, cadastrar (tipo Cachorro ou Gato), alterar, desativar e reativar. */
public class PainelRacas extends JPanel implements Atualizavel {

    private static final long serialVersionUID = 1L;

    private final RacaDAO dao = new RacaDAO();

    private final ModeloTabela<Raca> modelo = new ModeloTabela<>(
            new String[]{"ID", "Raça", "Tipo de animal", "Status"},
            r -> new Object[]{r.getId(), r.getNome(), r.getTipoAnimal(), Formatos.status(r.isStatus())});
    private final JTable tabela = Tema.tabela(modelo);
    private final BarraPesquisa barra = new BarraPesquisa("Pesquisar por nome da raça ou tipo", this::pesquisar);
    private final JLabel lblContagem = Tema.subtitulo(" ");

    private final JLabel lblFormulario = Tema.secao("Nova raça");
    private final JTextField txtId = Tema.campo();
    private final JTextField txtNome = Tema.campo();
    private final JComboBox<String> cmbTipo = new JComboBox<>(Raca.TIPOS);
    private final BotoesCrud botoes;

    private Raca selecionada;
    private boolean carregando;

    public PainelRacas() {
        setLayout(new BorderLayout(18, 14));
        setBackground(Tema.FUNDO);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        botoes = new BotoesCrud(true, this::novo, this::salvar, this::desativar, this::reativar);
        add(criarTopo(), BorderLayout.NORTH);
        add(Tema.rolagem(tabela), BorderLayout.CENTER);
        add(criarFormulario(), BorderLayout.EAST);

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
        Tema.estilizarCampo(cmbTipo);
        novo();
    }

    @Override
    public void atualizar() {
        pesquisar();
    }

    /** Usado pelo atalho do painel inicial. */
    public void novoCadastro() {
        novo();
        txtNome.requestFocusInWindow();
    }

    private JPanel criarTopo() {
        JPanel topo = new JPanel();
        topo.setBackground(Tema.FUNDO);
        topo.setLayout(new BoxLayout(topo, BoxLayout.Y_AXIS));


        JLabel titulo = Tema.titulo("Raças");
        titulo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = Tema.subtitulo("Cadastre as raças de cachorros e gatos usadas no cadastro de animais.");
        sub.setAlignmentX(LEFT_ALIGNMENT);

        lblContagem.setAlignmentX(LEFT_ALIGNMENT);
        barra.setAlignmentX(LEFT_ALIGNMENT);

        topo.add(titulo);
        topo.add(Box.createVerticalStrut(4));
        topo.add(sub);
        topo.add(Box.createVerticalStrut(12));
        topo.add(barra);
        topo.add(Box.createVerticalStrut(6));
        topo.add(lblContagem);
        return topo;
    }

    private JPanel criarFormulario() {
        JPanel cartao = Tema.cartao();
        cartao.setLayout(new BorderLayout(0, 14));
        cartao.setPreferredSize(new Dimension(320, 0));

        lblFormulario.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        cartao.add(lblFormulario, BorderLayout.NORTH);

        JPanel campos = new JPanel(new GridBagLayout());
        campos.setOpaque(false);

        adicionarCampo(campos, 0, "ID", txtId);
        adicionarCampo(campos, 1, "Nome da raça *", txtNome);
        adicionarCampo(campos, 2, "Tipo de animal *", cmbTipo);

        GridBagConstraints empurrar = Tema.gbc(0, 3);
        empurrar.weighty = 1.0;
        JPanel espaco = new JPanel();
        espaco.setOpaque(false);
        campos.add(espaco, empurrar);

        cartao.add(campos, BorderLayout.CENTER);
        cartao.add(botoes, BorderLayout.SOUTH);
        return cartao;
    }

    private void adicionarCampo(JPanel painel, int linha, String rotulo, JComponent componente) {
        JPanel grupo = new JPanel(new BorderLayout(0, 4));
        grupo.setOpaque(false);
        grupo.add(Tema.rotulo(rotulo), BorderLayout.NORTH);
        grupo.add(componente, BorderLayout.CENTER);

        GridBagConstraints c = Tema.gbc(0, linha);
        c.weightx = 1.0;
        painel.add(grupo, c);
    }

    private void pesquisar() {
        try {
            carregando = true;
            List<Raca> lista = dao.pesquisar(barra.getTexto(), barra.isMostrarInativos());
            modelo.setItens(lista);
            lblContagem.setText(lista.size() + (lista.size() == 1 ? " raça encontrada" : " raças encontradas"));
            botoes.atualizar(selecionada != null, barra.isMostrarInativos());
        } catch (DAOException e) {
            Mensagens.erro(this, "Erro ao consultar raças: " + e.getMessage());
        } finally {
            carregando = false;
        }
    }

    private void novo() {
        selecionada = null;
        tabela.clearSelection();
        lblFormulario.setText("Nova raça");
        txtId.setText("");
        txtNome.setText("");
        if (cmbTipo.getItemCount() > 0) {
            cmbTipo.setSelectedIndex(0);
        }
        botoes.atualizar(false, barra.isMostrarInativos());
        txtNome.requestFocus();
    }

    private void carregarNoFormulario(Raca r) {
        selecionada = r;
        lblFormulario.setText("Editando raça #" + r.getId());
        txtId.setText(String.valueOf(r.getId()));
        txtNome.setText(Formatos.vazioSeNulo(r.getNome()));
        cmbTipo.setSelectedItem(r.getTipoAnimal());
        botoes.atualizar(true, barra.isMostrarInativos());
    }

    private void salvar() {
        try {
            String nome = txtNome.getText().trim();
            String tipo = (String) cmbTipo.getSelectedItem();

            if (nome.isEmpty()) {
                Mensagens.aviso(this, "Informe o nome da raça.");
                txtNome.requestFocus();
                return;
            }
            if (tipo == null || tipo.isBlank()) {
                Mensagens.aviso(this, "Selecione o tipo de animal.");
                return;
            }

            Raca r = (selecionada != null) ? selecionada : new Raca();
            r.setNome(nome);
            r.setTipoAnimal(tipo);

            if (selecionada == null) {
                dao.inserir(r);
                JOptionPane.showMessageDialog(this, "Raça cadastrada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                dao.atualizar(r);
                JOptionPane.showMessageDialog(this, "Raça atualizada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }
            novo();
            pesquisar();
        } catch (DAOException e) {
            Mensagens.erro(this, "Erro ao salvar raça: " + e.getMessage());
        }
    }

    private void desativar() {
        if (selecionada == null) {
            return;
        }
        if (Mensagens.confirmar(this, "Deseja desativar a raça \"" + selecionada.getNome() + "\"?")) {
            try {
                dao.desativar(selecionada.getId());
                novo();
                pesquisar();
            } catch (DAOException e) {
                Mensagens.erro(this, "Erro ao desativar raça: " + e.getMessage());
            }
        }
    }

    private void reativar() {
        if (selecionada == null) {
            return;
        }
        if (Mensagens.confirmar(this, "Deseja reativar a raça \"" + selecionada.getNome() + "\"?")) {
            try {
                dao.reativar(selecionada.getId());
                novo();
                pesquisar();
            } catch (DAOException e) {
                Mensagens.erro(this, "Erro ao reativar raça: " + e.getMessage());
            }
        }
    }
}
