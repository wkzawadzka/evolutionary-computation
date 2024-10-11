import pandas as pd
import sys
from docx import Document
from docx.shared import Inches
from read_solution_files import *
from tables import *
from docx.shared import Pt
from datetime import datetime
from docx.enum.text import WD_PARAGRAPH_ALIGNMENT


def add_table(doc, summary_df):
    # Create a table with the number of rows equal to the length of the DataFrame plus one for the header
    table = doc.add_table(rows=summary_df.shape[0] + 1, cols=len(summary_df.columns))

    # Add the header row
    hdr_cells = table.rows[0].cells
    for i, column in enumerate(summary_df.columns):
        hdr_cells[i].text = column

    # Add the data rows
    for i, row in summary_df.iterrows():
        for j, value in enumerate(row):
            table.cell(i + 1, j).text = str(value)

def save_doc(doc, week_name):
    doc_path = f'../../reports/{week_name}.docx'
    doc.save(doc_path)
    print(f"Summary report saved to '{doc_path}'.")

def create_summary_report(df, week_name):
    doc = Document()

    # authors
    authors_paragraph = doc.add_paragraph()
    authors_run = authors_paragraph.add_run('Authors: Weronika Zawadzka 151943 Eliza Czaplicka 151963') 
    authors_run.font.size = Pt(8) 
    authors_paragraph.alignment = WD_PARAGRAPH_ALIGNMENT.RIGHT

    # date
    date_paragraph = doc.add_paragraph()
    date_run = date_paragraph.add_run(f'Date: {datetime.now().strftime("%B %d, %Y")}')
    date_run.font.size = Pt(8) 
    date_paragraph.alignment = WD_PARAGRAPH_ALIGNMENT.RIGHT

    doc.add_heading(f'Summary Report for {week_name}', level=1)

    # Create summary statistics tables
    for instance in df['instance'].unique():
        doc.add_heading(f'Instance: {instance}', level=2)
        variable_labels, summary_stats = create_summary_table(df, instance)

        for variable, summary_df in summary_stats.items():
            doc.add_heading(f'Summary for {variable_labels[variable]}', level=3)
            add_table(doc, summary_df)

        doc.add_page_break()

    # Save the document
    save_doc(doc, week_name)


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python create_report.py <week_name> \n e.g. python create_report.py w1_greedy_heuristics")
        sys.exit(1)

    week_name = sys.argv[1]
    results = read_solution_files(week_name)

    create_summary_report(results, week_name)
