package playerWeight.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import playerWeight.api.WeightRegistry;
import playerWeight.handler.ClientHandler;
import playerWeight.ui.typeEntry.ITypeEntry;
import playerWeight.ui.typeEntry.ITypeEntry.SorterType;

public class HelperUI extends JFrame
{
	JPanel contentPane;
	JTable table;
	CustomTableModel model;
	JScrollPane scrollPane;
	JButton setSizeButton;
	JButton resetButton;
	final ButtonGroup TypeGroup = new ButtonGroup();
	int currentType = -1;
	final List<ITypeEntry> currentList = new ArrayList<ITypeEntry>();
	private JButton giveItem;
	private JLabel defaultWeight;
	private JLabel lblDefaultPlayerWeight;
	private JComboBox<SorterType> comboBox;
	private JCheckBox invertSorter;

	
	public HelperUI()
	{
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int close = JOptionPane.showConfirmDialog(null, "Do you really want to close this window. (You have to restart the Game to open it again)", "Close Window", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(close == 0)
				{
					if(ChangeRegistry.INSTANCE.hasChanges())
					{
						close = JOptionPane.showConfirmDialog(null, "You have Unexported Changes. These are lost. Do you really want to close this window?", "Close Window", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						if(close != 0)
						{
							return;
						}
					}
					HelperUI.this.dispose();
				}
			}
		});
		initUI();
		onListChange(0);
	}
	
	private void initUI()
	{
		setResizable(false);
		setTitle("CustomParser");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 1053, 541);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(5, 5, 683, 486);
		contentPane.add(scrollPane);
		
		model = new CustomTableModel(currentList, 0);
		table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrollPane.setViewportView(table);
		
		JRadioButton items = new JRadioButton("Items", true);
		items.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				onListChange(0);
			}
		});
		TypeGroup.add(items);
		items.setBounds(698, 8, 58, 23);
		contentPane.add(items);
		
		JRadioButton itemstacks = new JRadioButton("ItemStacks");
		itemstacks.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				onListChange(1);
			}
		});
		TypeGroup.add(itemstacks);
		itemstacks.setBounds(698, 34, 92, 23);
		contentPane.add(itemstacks);
		
		JRadioButton oredict = new JRadioButton("OreDictionary");
		oredict.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				onListChange(2);
			}
		});
		TypeGroup.add(oredict);
		oredict.setBounds(698, 84, 109, 23);
		contentPane.add(oredict);
		
		JRadioButton fluids = new JRadioButton("Fluids");
		fluids.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				onListChange(3);
			}
		});
		TypeGroup.add(fluids);
		fluids.setBounds(698, 59, 73, 23);
		contentPane.add(fluids);
		
		JButton btnSetWeight = new JButton("Set Weight");
		btnSetWeight.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int[] selection = getTable().getSelectedRows();
				if(selection.length == 0)
				{
					JOptionPane.showConfirmDialog(null, "You need to select Entries to change!", "Setting Weight", JOptionPane.CLOSED_OPTION, JOptionPane.ERROR_MESSAGE);
					return;
				}
				String value = JOptionPane.showInputDialog(null, "Please type in the Weight", "Setting Weight", JOptionPane.QUESTION_MESSAGE);
				try
				{
					double result = Double.parseDouble(value);
					List<ITypeEntry> type = getModel().getList();
					for(int i : selection)
					{
						removeChange(type.get(i));
						type.get(i).setWeight(result);
						addChange(type.get(i));
					}
					getModel().fireTableDataChanged();
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
					JOptionPane.showConfirmDialog(null, "Number can not be parsed", "Setting Weight", JOptionPane.CLOSED_OPTION, JOptionPane.ERROR_MESSAGE);
				}
			}
			
		});
		btnSetWeight.setBounds(698, 129, 128, 23);
		contentPane.add(btnSetWeight);
		
		setSizeButton = new JButton("Set Max Size");
		setSizeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int[] selection = getTable().getSelectedRows();
				if(selection.length == 0)
				{
					JOptionPane.showConfirmDialog(null, "You need to select Entries to change!", "Setting Max Size", JOptionPane.CLOSED_OPTION, JOptionPane.ERROR_MESSAGE);
					return;
				}
				String value = JOptionPane.showInputDialog(null, "Please type in the Max StackSize", "Setting Max Size", JOptionPane.QUESTION_MESSAGE);
				try
				{
					int result = Integer.parseInt(value);
					if(result < 1 || result > 64)
					{
						JOptionPane.showConfirmDialog(null, "Value needs to be between 1-64", "Setting Max Size", JOptionPane.CLOSED_OPTION, JOptionPane.ERROR_MESSAGE);
						return;
					}
					List<ITypeEntry> type = getModel().getList();
					for(int i : selection)
					{
						removeSize(type.get(i));
						type.get(i).setSize(result);
						addSize(type.get(i));
					}
					getModel().fireTableDataChanged();
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
					JOptionPane.showConfirmDialog(null, "Number can not be parsed", "Setting Weight", JOptionPane.CLOSED_OPTION, JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		setSizeButton.setBounds(698, 163, 128, 23);
		contentPane.add(setSizeButton);
		
		JButton btnNewButton = new JButton("Clear Weight");
		btnNewButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int[] selection = getTable().getSelectedRows();
				if(selection.length == 0)
				{
					JOptionPane.showConfirmDialog(null, "You need to select Entries to change!", "Clear Weight", JOptionPane.CLOSED_OPTION, JOptionPane.ERROR_MESSAGE);
					return;
				}
				List<ITypeEntry> type = getModel().getList();
				for(int i : selection)
				{
					removeChange(type.get(i));
					type.get(i).setWeight(0D);
				}
				getModel().fireTableDataChanged();
			}
		});
		btnNewButton.setBounds(836, 129, 128, 23);
		contentPane.add(btnNewButton);
		
		resetButton = new JButton("Reset Max Size");
		resetButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int[] selection = getTable().getSelectedRows();
				if(selection.length == 0)
				{
					JOptionPane.showConfirmDialog(null, "You need to select Entries to change!", "Reseting Max Size", JOptionPane.CLOSED_OPTION, JOptionPane.ERROR_MESSAGE);
					return;
				}
				List<ITypeEntry> type = getModel().getList();
				for(int i : selection)
				{
					removeSize(type.get(i));
					type.get(i).setSize(0);
				}
				getModel().fireTableDataChanged();
			}
		});
		resetButton.setBounds(836, 163, 128, 23);
		contentPane.add(resetButton);
		
		giveItem = new JButton("Give Item");
		giveItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int[] selection = getTable().getSelectedRows();
				if(selection.length == 0)
				{
					JOptionPane.showConfirmDialog(null, "You need to select Entries to change!", "Give Item", JOptionPane.CLOSED_OPTION, JOptionPane.ERROR_MESSAGE);
					return;
				}
				List<ItemStack> list = new ArrayList<ItemStack>();
				List<ITypeEntry> type = getModel().getList();
				for(int i : selection)
				{
					ItemStack stack = type.get(i).makeStack();
					if(stack.isEmpty())
					{
						continue;
					}
					list.add(stack.copy());
				}
				givePlayerItems(list);
			}
		});
		giveItem.setBounds(698, 197, 128, 23);
		contentPane.add(giveItem);
		
		JButton btnNewButton_1 = new JButton("Export Changes");
		btnNewButton_1.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String name = JOptionPane.showInputDialog(null, "Please Type in the File name to Export to (It gets overriden)", "Export Changes", JOptionPane.QUESTION_MESSAGE);
				if(name == null || name.isEmpty())
				{
					return;
				}
				int delete = JOptionPane.showConfirmDialog(null, "Do you want clear all Cached changes after export? (Effects only next Change Export)", "Export Changes", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				ChangeRegistry.INSTANCE.exportChanges(name + ".xml", delete == 0);
			}
		});
		btnNewButton_1.setBounds(774, 468, 132, 23);
		contentPane.add(btnNewButton_1);
		
		JButton btnNewButton_2 = new JButton("Load loaded Changes");
		btnNewButton_2.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int total = ChangeRegistry.INSTANCE.loadChanges();
				JOptionPane.showConfirmDialog(null, "Loaded " + total + " Changes from Existing Files", "Load Changes", JOptionPane.CLOSED_OPTION, JOptionPane.INFORMATION_MESSAGE);
			}
		});
		btnNewButton_2.setBounds(749, 434, 178, 23);
		contentPane.add(btnNewButton_2);
		
		JButton btnNewButton_3 = new JButton("Set Default Weight");
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String value = JOptionPane.showInputDialog(null, "Please type in the Weight", "Setting Default Weight", JOptionPane.QUESTION_MESSAGE);
				try
				{
					ChangeRegistry.INSTANCE.removeChange("<type=defaultWeight weight="+WeightRegistry.INSTANCE.getDefaultWeight()+">");
					double result = Double.parseDouble(value);
					WeightRegistry.INSTANCE.setDefaultWeight(result);
					ChangeRegistry.INSTANCE.addChange("<type=defaultWeight weight="+WeightRegistry.INSTANCE.getDefaultWeight()+">");
					defaultWeight.setText("Default-Weight: "+ClientHandler.createToolTip(WeightRegistry.INSTANCE.getDefaultWeight()));
				}
				catch(Exception e2)
				{
					e2.printStackTrace();
					JOptionPane.showConfirmDialog(null, "Number can not be parsed", "Setting Default Weight", JOptionPane.CLOSED_OPTION, JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnNewButton_3.setBounds(698, 231, 171, 23);
		contentPane.add(btnNewButton_3);
		
		defaultWeight = new JLabel("Default-Weight: "+ClientHandler.createToolTip(WeightRegistry.INSTANCE.getDefaultWeight()));
		defaultWeight.setBounds(698, 265, 171, 14);
		contentPane.add(defaultWeight);
		
		JButton btnNewButton_4 = new JButton("Set Player Weight");
		btnNewButton_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String value = JOptionPane.showInputDialog(null, "Please type in the Weight", "Setting Player Weight", JOptionPane.QUESTION_MESSAGE);
				try
				{
					ChangeRegistry.INSTANCE.removeChange("<type=defaultPlayerWeight weight="+WeightRegistry.INSTANCE.getDefaultPlayerWeight()+">");
					double result = Double.parseDouble(value);
					WeightRegistry.INSTANCE.setPlayerDefaultWeight(result);
					ChangeRegistry.INSTANCE.addChange("<type=defaultPlayerWeight weight="+WeightRegistry.INSTANCE.getDefaultPlayerWeight()+">");
					lblDefaultPlayerWeight.setText("Default Player Weight: "+ClientHandler.createToolTip(WeightRegistry.INSTANCE.getDefaultPlayerWeight()));
				}
				catch(Exception e2)
				{
					e2.printStackTrace();
					JOptionPane.showConfirmDialog(null, "Number can not be parsed", "Setting Player Weight", JOptionPane.CLOSED_OPTION, JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnNewButton_4.setBounds(698, 290, 171, 23);
		contentPane.add(btnNewButton_4);
		lblDefaultPlayerWeight = new JLabel("Default Player Weight: "+ClientHandler.createToolTip(WeightRegistry.INSTANCE.getDefaultPlayerWeight()));
		lblDefaultPlayerWeight.setBounds(698, 324, 171, 14);
		contentPane.add(lblDefaultPlayerWeight);
		
		comboBox = new JComboBox<SorterType>();
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.sort(comboBox.getItemAt(comboBox.getSelectedIndex()), invertSorter.isSelected());
			}
		});
		comboBox.setModel(new DefaultComboBoxModel(createSorterByType(0)));
		comboBox.setBounds(698, 349, 128, 20);
		contentPane.add(comboBox);
		
		invertSorter = new JCheckBox("Inverted Sorter?");
		invertSorter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.sort(comboBox.getItemAt(comboBox.getSelectedIndex()), invertSorter.isSelected());
			}
		});
		invertSorter.setBounds(836, 345, 128, 23);
		contentPane.add(invertSorter);
	}
	
	public void onListChange(int type)
	{
		if(currentType != type)
		{
			currentType = type;
			currentList.clear();
			ChangeRegistry.INSTANCE.addToList(currentType, currentList);
			model = new CustomTableModel(currentList, type);
			table.setModel(model);
			setSizeButton.setEnabled(type == 0);
			resetButton.setEnabled(type == 0);
			giveItem.setEnabled(type == 0 || type == 1);
			if(type == 0)
			{
				table.getColumnModel().getColumn(2).setMaxWidth(100);
				table.getColumnModel().getColumn(3).setMaxWidth(100);
				table.getColumnModel().getColumn(4).setMaxWidth(100);
				table.getColumnModel().getColumn(4).setPreferredWidth(100);
			}
			comboBox.setModel(new DefaultComboBoxModel(createSorterByType(type)));
		}
	}
	
	SorterType[] createSorterByType(int type)
	{
		if(type == 0) return new SorterType[]{SorterType.ID, SorterType.Mod, SorterType.Name, SorterType.Weight, SorterType.ItemSize};
		else if(type == 1) return new SorterType[]{SorterType.ID, SorterType.IDMeta, SorterType.Mod, SorterType.Name, SorterType.Weight};
		else if(type == 2) return new SorterType[]{SorterType.ID, SorterType.Name, SorterType.Weight};
		else if(type == 2) return new SorterType[]{SorterType.ID, SorterType.Mod, SorterType.Name, SorterType.Weight};
		return new SorterType[0];
	}
	
	final JTable getTable()
	{
		return table;
	}
	
	final CustomTableModel getModel()
	{
		return model;
	}
	
	void removeChange(ITypeEntry entry)
	{
		if(entry.isChanged(false))
		{
			ChangeRegistry.INSTANCE.removeChange(entry.makeChange(false));
		}
		for(ITypeEntry subEntry : entry.getSubEntries())
		{
			if(subEntry.isChanged(false))
			{
				ChangeRegistry.INSTANCE.removeChange(subEntry.makeChange(false));
			}
		}
	}
	
	void addChange(ITypeEntry entry)
	{
		if(entry.isChanged(false))
		{
			ChangeRegistry.INSTANCE.addChange(entry.makeChange(false));
		}
	}
	
	void removeSize(ITypeEntry entry)
	{
		if(entry.isChanged(true))
		{
			ChangeRegistry.INSTANCE.removeChange(entry.makeChange(false));
		}
	}
	
	void addSize(ITypeEntry entry)
	{
		if(entry.isChanged(true))
		{
			ChangeRegistry.INSTANCE.addChange(entry.makeChange(false));
		}
	}
	
	@SideOnly(Side.CLIENT)
	void givePlayerItems(List<ItemStack> list)
	{
		EntityPlayer player = Minecraft.getMinecraft().player;
		if(player == null)
		{
			JOptionPane.showConfirmDialog(null, "Player needs to exist!", "Give Item", JOptionPane.CLOSED_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(player.getUniqueID());
		if(player == null)
		{
			JOptionPane.showConfirmDialog(null, "Player needs to exist!", "Give Item", JOptionPane.CLOSED_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		for(ItemStack stack : list)
		{
			if(!player.inventory.addItemStackToInventory(stack))
			{
				player.dropItem(stack, false);
			}
		}
	}
}
